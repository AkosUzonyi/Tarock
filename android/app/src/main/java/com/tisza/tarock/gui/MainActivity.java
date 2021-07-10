package com.tisza.tarock.gui;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.core.app.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;
import com.tisza.tarock.R;
import com.tisza.tarock.proto.*;

public class MainActivity extends AppCompatActivity implements GameListAdapter.GameAdapterListener
{
	private static final int DISCONNECT_DELAY_SEC = 40;
	private static final int DOWNLOAD_CSV_REQUEST_CODE = 1;

	private ConnectionViewModel connectionViewModel;
	private ProgressDialog progressDialog;

	private Handler handler;
	private Runnable disconnectRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null);

		LocaleManager.updateLocale(this);
		setContentView(R.layout.main);

		connectionViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
		connectionViewModel.getConnectionState().observe(this, this::connectionStateChanged);
		connectionViewModel.getErrorState().observe(this, this::error);
		progressDialog = new ProgressDialog(this);
		handler = new Handler();
		disconnectRunnable = connectionViewModel::disconnect;

		LoginFragment loginFragment = new LoginFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.fragment_container, loginFragment, LoginFragment.TAG)
				.commit();

		if (getIntent().hasExtra(GameFragment.KEY_GAME_SESSION_ID))
			connectionViewModel.login();

		connectionViewModel.getHistoryGameSessionID().observe(this, gameSessionID ->
		{
			if (gameSessionID < 0)
				joinGameSession(gameSessionID);
		});
	}

	public void openSettings()
	{
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new SettingsFragment(), SettingsFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void openDonationFragment()
	{
		DonationFragment donationFragment = new DonationFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, donationFragment, DonationFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	private void connectionStateChanged(ConnectionViewModel.ConnectionState connectionState)
	{
		switch (connectionState)
		{
			case DISCONNECTED:
			case CONNECTED:
				popBackToLoginScreen();
				break;
			case CONNECTING:
				progressDialog.setMessage(getResources().getString(R.string.connecting));
				progressDialog.setOnDismissListener(x -> connectionViewModel.disconnect());
				progressDialog.show();
				break;
			case LOGGING_IN:
				progressDialog.setMessage(getResources().getString(R.string.logging_in));
				progressDialog.show();
				break;
			case LOGGED_IN:
				onSuccessfulLogin();
				break;
		}
	}

	private void error(ConnectionViewModel.ErrorState errorState)
	{
		if (errorState == null)
			return;

		switch (errorState)
		{
			case OUTDATED:
				requireUpdate();
				break;
			case NO_NETWORK:
				showErrorDialog(R.string.error_no_network_title, R.string.error_no_network_message);
				break;
			case SERVER_DOWN:
				showErrorDialog(R.string.error_server_down_title, R.string.error_server_down_message);
				break;
			case SERVER_ERROR:
				showErrorDialog(R.string.error_server_error_title, R.string.error_server_error_message);
				break;
			case LOGIN_UNSUCCESSFUL:
				showErrorDialog(R.string.error_login_unsuccessful_title, R.string.error_login_unsuccessful_message);
				break;
		}
	}

	private void showErrorDialog(int title, int message)
	{
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setNeutralButton(R.string.ok, (dialog, which) -> {})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}

	private void onSuccessfulLogin()
	{
		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
		if (currentFragment != null && !LoginFragment.TAG.equals(currentFragment.getTag()))
			return;

		popBackToLoginScreen();

		GameListFragment gameListFragment = new GameListFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameListFragment, GameListFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();

		String gameSessionID = getIntent().getStringExtra(GameFragment.KEY_GAME_SESSION_ID);
		if (gameSessionID != null)
		{
			getIntent().removeExtra(GameFragment.KEY_GAME_SESSION_ID);
			joinGameSession(Integer.parseInt(gameSessionID));
		}

		String historyGameID = getIntent().getStringExtra("hgid");
		if (historyGameID != null)
		{
			getIntent().removeExtra("hgid");
			viewHistoryGame(Integer.parseInt(historyGameID));
		}
	}

	public void createNewGame()
	{
		CreateGameFragment createGameFragment = new CreateGameFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, createGameFragment, CreateGameFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void joinGameSession(int gameSessionID)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt(GameFragment.KEY_GAME_SESSION_ID, gameSessionID);
		gameFragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameFragment, GameFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	public void viewHistoryGame(int gameSessionID)
	{
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setJoinHistoryGame(MainProto.JoinHistoryGame.newBuilder().setGameId(gameSessionID)).build());
	}

	@Override
	public void deleteGame(int gameSessionID)
	{
		MainProto.Message deleteMessage = MainProto.Message.newBuilder().setDeleteGameSession(MainProto.DeleteGameSession.newBuilder().setGameSessionId(gameSessionID)).build();

		new AlertDialog.Builder(this)
				.setTitle(R.string.delete_game_confirm)
				.setPositiveButton(R.string.delete_game, (dialog, which) -> connectionViewModel.sendMessage(deleteMessage))
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	public void downloadCsv()
	{
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
		    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_CSV_REQUEST_CODE);
			return;
		}

		DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse("https://tarokk.net/cgi-bin/tarock/tarokk_pontok.csv?user_id=" + connectionViewModel.getUserID().getValue()));
		downloadRequest.allowScanningByMediaScanner();
		downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "tarokk_pontok.csv");
		DownloadManager downloadmanager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
		downloadmanager.enqueue(downloadRequest);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == DOWNLOAD_CSV_REQUEST_CODE)
		{
			for (int grantResult : grantResults)
				if (grantResult != PackageManager.PERMISSION_GRANTED)
					return;

			downloadCsv();
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		handler.removeCallbacks(disconnectRunnable);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		handler.postDelayed(disconnectRunnable,  DISCONNECT_DELAY_SEC * 1000);
	}

	private void popBackToLoginScreen()
	{
		progressDialog.setOnDismissListener(null);
		if (progressDialog.isShowing())
			progressDialog.dismiss();

		Fragment loginFragment = getSupportFragmentManager().findFragmentByTag(LoginFragment.TAG);
		if (loginFragment == null)
			loginFragment = new LoginFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, loginFragment, LoginFragment.TAG)
				.commit();
	}

	private void requireUpdate()
	{
		new AlertDialog.Builder(this)
				.setTitle(R.string.update_available)
				.setMessage(R.string.update_please)
				.setPositiveButton(R.string.update_accept, (dialog, which) ->
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()))))
				.setNegativeButton(R.string.update_deny, (dialog, which) ->
						finish())
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
}
