package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.*;
import com.facebook.*;
import com.facebook.login.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.tasks.*;
import com.tisza.tarock.R;
import com.tisza.tarock.proto.*;

public class MainActivity extends AppCompatActivity implements GameListAdapter.GameAdapterListener
{
	private static final int DISCONNECT_DELAY_SEC = 40;

	private ConnectionViewModel connectionViewModel;
	private ProgressDialog progressDialog;

	private Handler handler;
	private Runnable disconnectRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		connectionViewModel = ViewModelProviders.of(this).get(ConnectionViewModel.class);
		connectionViewModel.getConnectionState().observe(this, this::connectionStateChanged);
		connectionViewModel.getErrorState().observe(this, this::error);
		progressDialog = new ProgressDialog(this);
		handler = new Handler();
		disconnectRunnable = connectionViewModel::disconnect;

		if (savedInstanceState == null)
		{
			LoginFragment loginFragment = new LoginFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, loginFragment, LoginFragment.TAG)
					.commit();

			if (getIntent().hasExtra(GameFragment.KEY_GAME_ID))
				connectionViewModel.login();
		}
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
		joinGame(gameID, false);
	}

	public void joinGame(int gameID, boolean history)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt(history ? GameFragment.KEY_HISTORY_GAME_ID : GameFragment.KEY_GAME_ID, gameID);
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
		connectionViewModel.sendMessage(MainProto.Message.newBuilder().setDeleteGameSession(MainProto.DeleteGameSession.newBuilder()
				.setGameSessionId(gameID))
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
