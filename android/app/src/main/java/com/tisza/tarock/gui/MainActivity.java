package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;
import com.facebook.*;
import com.tisza.tarock.R;
import com.tisza.tarock.proto.*;

public class MainActivity extends AppCompatActivity implements GameListAdapter.GameAdapterListener
{
	private static final int DISCONNECT_DELAY_SEC = 40;

	private CallbackManager callbackManager;

	private ConnectionViewModel connectionViewModel;
	private ProgressDialog progressDialog;

	private Handler handler;
	private Runnable disconnectRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		ResourceMappings.init(this);
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

		if (getIntent().hasExtra(GameFragment.KEY_GAME_ID))
			connectionViewModel.login();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private void onSuccessfulLogin()
	{
		popBackToLoginScreen();

		GameListFragment gameListFragment = new GameListFragment();

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameListFragment, GameListFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();

		String gameID = getIntent().getStringExtra(GameFragment.KEY_GAME_ID);
		if (gameID != null)
		{
			getIntent().removeExtra(GameFragment.KEY_GAME_ID);
			joinGame(Integer.parseInt(gameID));
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

	@Override
	public void joinGame(int gameID)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt(GameFragment.KEY_GAME_ID, gameID);
		gameFragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameFragment, GameFragment.TAG)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void deleteGame(int gameID)
	{
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setDeleteGame(MainProto.DeleteGame.newBuilder()
				.setGameId(gameID))
				.build());
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

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new LoginFragment(), LoginFragment.TAG)
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
