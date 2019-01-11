package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import com.facebook.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.iid.*;
import com.tisza.tarock.BuildConfig;
import com.tisza.tarock.R;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity implements MessageHandler, GameListAdapter.GameAdapterListener
{
	private static final int DISCONNECT_DELAY_SEC = 40;

	private Executor uiThreadExecutor;

	private CallbackManager callbackManager;

	private boolean started = false;

	private ProgressDialog progressDialog;

	private boolean loggedIn = false;
	private ProtoConnection connection;
	private ActionSender actionSender;
	private Collection<EventHandler> eventHandlers = new ArrayList<>();

	private GameListAdapter gameListAdapter;
	private AvailableUsersAdapter availableUsersAdapter;

	private Handler handler;
	private final Runnable disconnectRunnable = this::disconnect;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(this.getApplicationContext());
		callbackManager = CallbackManager.Factory.create();

		ResourceMappings.init(this);
		uiThreadExecutor = new UIThreadExecutor();
		setContentView(R.layout.main);
		progressDialog = new ProgressDialog(this);
		handler = new Handler();

		gameListAdapter = new GameListAdapter(this, this);
		availableUsersAdapter = new AvailableUsersAdapter(this);

		LoginFragment loginFragment = new LoginFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.fragment_container, loginFragment, "login")
				.commit();

		if (getIntent().hasExtra("game_id"))
			onPlayButtonClicked();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	public void onPlayButtonClicked()
	{
		if (connection != null)
		{
			login();
			return;
		}

		new ConnectAsyncTask().execute();
	}

	private void login()
	{
		if (loggedIn)
		{
			onSuccesfulLogin();
			return;
		}

		connection.sendMessage(MainProto.Message.newBuilder().setLogin(MainProto.Login.newBuilder()
				.setFacebookToken(AccessToken.getCurrentAccessToken().getToken())
				.build())
				.build());

		progressDialog.setMessage(getResources().getString(R.string.logging_in));
		progressDialog.show();
	}

	private void onSuccesfulLogin()
	{
		popBackToLoginScreen();

		FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult ->
		{
			if (loggedIn)
				connection.sendMessage(MainProto.Message.newBuilder().setFcmToken(MainProto.FCMToken.newBuilder()
						.setFcmToken(instanceIdResult.getToken())
						.setActive(true)
						.build()).build());
		});

		GameListFragment gameListFragment = new GameListFragment();

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameListFragment, "gamelist")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();

		String gameID = getIntent().getStringExtra("game_id");
		if (gameID != null)
		{
			getIntent().removeExtra("game_id");
			joinGame(Integer.parseInt(gameID));
		}
	}

	public void createNewGame()
	{
		CreateGameFragment createGameFragment = new CreateGameFragment();

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, createGameFragment, "create_game")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void joinGame(int gameID)
	{
		GameFragment gameFragment = new GameFragment();

		Bundle args = new Bundle();
		args.putInt("gameID", gameID);
		gameFragment.setArguments(args);

		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, gameFragment, "game")
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(null)
				.commit();
	}

	@Override
	public void deleteGame(int gameID)
	{
		connection.sendMessage(MainProto.Message.newBuilder().setDeleteGame(MainProto.DeleteGame.newBuilder()
				.setGameId(gameID))
				.build());
	}

	public void disconnect()
	{
		if (connection != null)
			new DisconnectAsyncTask().execute();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		started = true;
		handler.removeCallbacks(disconnectRunnable);

		if (connection == null)
			popBackToLoginScreen();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		started = false;
		handler.postDelayed(disconnectRunnable,  DISCONNECT_DELAY_SEC * 1000);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		disconnect();
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		switch (message.getMessageTypeCase())
		{
			case LOGIN:
				loggedIn = message.getLogin().hasFacebookToken();

				if (loggedIn)
				{
					onSuccesfulLogin();
				}
				else
				{
					popBackToLoginScreen();
				}

				if (progressDialog.isShowing())
					progressDialog.dismiss();

				break;

			case EVENT:
				for (EventHandler eventHandler : eventHandlers)
					new ProtoEvent(message.getEvent()).handle(eventHandler);

				break;

			case SERVER_STATUS:
				List<GameInfo> games = new ArrayList<>();
				for (MainProto.Game gameProto : message.getServerStatus().getAvailableGameList())
				{
					games.add(Utils.gameInfoFromProto(gameProto));
				}

				List<User> users = new ArrayList<>();
				for (MainProto.User userProto : message.getServerStatus().getAvailableUserList())
				{
					users.add(Utils.userFromProto(userProto));
				}

				gameListAdapter.setGames(games);
				availableUsersAdapter.setUsers(users);

				break;

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	@Override
	public void connectionError(ErrorType errorType)
	{
		switch (errorType)
		{
			case INVALID_HELLO:
				break;
			case VERSION_MISMATCH:
				requireUpdate();
				break;
		}

		if (progressDialog.isShowing())
			progressDialog.dismiss();

		connection = null;
	}

	private void popBackToLoginScreen()
	{
		getFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, new LoginFragment(), "login")
				.commit();
	}

	@Override
	public void connectionClosed()
	{
		connection = null;
		actionSender = null;
		loggedIn = false;

		if (!started)
			return;

		popBackToLoginScreen();
	}

	public ActionSender getActionSender()
	{
		return actionSender;
	}

	public ProtoConnection getConnection()
	{
		return connection;
	}

	public void addEventHandler(EventHandler eventHandler)
	{
		eventHandlers.add(eventHandler);
	}

	public void removeEventHandler(EventHandler eventHandler)
	{
		eventHandlers.remove(eventHandler);
	}

	public GameListAdapter getGameListAdapter()
	{
		return gameListAdapter;
	}

	public AvailableUsersAdapter getAvailableUsersAdapter()
	{
		return availableUsersAdapter;
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

	private class DisconnectAsyncTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... voids)
		{
			if (connection != null)
			{
				try
				{
					connection.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				connection = null;
			}

			return null;
		}
	}

	private class ConnectAsyncTask extends AsyncTask<Void, Void, ProtoConnection> implements DialogInterface.OnDismissListener
	{
		@Override
		public void onDismiss(DialogInterface dialog)
		{
			cancel(true);
		}

		@Override
		protected void onPreExecute()
		{
			progressDialog.setMessage(getResources().getString(R.string.connecting));
			progressDialog.setOnDismissListener(this);
			progressDialog.show();
		}

		protected ProtoConnection doInBackground(Void... voids)
		{
			final String host = BuildConfig.DEBUG ? "dell" : "akos0.ddns.net";
			final int port = 8128;

			try
			{
				String trustStoreFile = "truststore.bks";

				KeyStore ks = KeyStore.getInstance("BKS");
				ks.load(getAssets().open(trustStoreFile), "000000".toCharArray());

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(ks, "000000".toCharArray());

				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(ks);

				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

				Socket socket = sc.getSocketFactory().createSocket();
				socket.connect(new InetSocketAddress(host, port), 1000);

				ProtoConnection protoConnection = new ProtoConnection(socket, uiThreadExecutor);
				return protoConnection;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}

		protected void onPostExecute(ProtoConnection resultProtoConnection)
		{
			progressDialog.setOnDismissListener(null);

			if (progressDialog.isShowing())
				progressDialog.dismiss();

			if (resultProtoConnection == null)
				return;

			connection = resultProtoConnection;
			connection.addMessageHandler(MainActivity.this);
			connection.start();
			actionSender = new ProtoActionSender(connection);
			login();
		}
	}
}
