package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.os.*;
import com.facebook.*;
import com.tisza.tarock.*;
import com.tisza.tarock.R;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity implements MessageHandler, GameListAdapter.GameAdapterListener
{
	private LoginFragment loginFragment = new LoginFragment();

	private ProtoConnection connection;
	private ActionSender actionSender;
	private EventHandler eventHandler;

	private List<GameInfo> games = new ArrayList<>();
	private List<User> users = new ArrayList<>();
	private GameListAdapter gameListAdapter;
	private AvailableUsersAdapter availableUsersAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		FacebookSdk.sdkInitialize(this.getApplicationContext());

		setContentView(R.layout.main);

		gameListAdapter = new GameListAdapter(this, this);
		availableUsersAdapter = new AvailableUsersAdapter(this);

		getFragmentManager().beginTransaction()
				.add(R.id.fragment_container, loginFragment, "login")
				.commit();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		loginFragment.onActivityResult(requestCode, resultCode, data);
	}

	public void login()
	{
		new ConnectAsyncTask().execute();
	}

	public void createNewGame()
	{
		CreateGameFragment createGameFragment = new CreateGameFragment();

		getFragmentManager().beginTransaction()
				.add(R.id.fragment_container, createGameFragment, "create_game")
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
				.add(R.id.fragment_container, gameFragment, "game")
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

	@Override
	public void onDestroy()
	{
		super.onDestroy();
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
		}
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		switch (message.getMessageTypeCase())
		{
			case EVENT:
				runOnUiThread(() -> new ProtoEvent(message.getEvent()).handle(eventHandler));
				break;

			case SERVER_STATUS:
				games.clear();
				for (MainProto.Game gameProto : message.getServerStatus().getAvailableGameList())
				{
					games.add(Utils.gameInfoFromProto(gameProto));
				}

				users.clear();
				for (MainProto.User userProto : message.getServerStatus().getAvailableUserList())
				{
					users.add(Utils.userFromProto(userProto));
				}

				runOnUiThread(() ->
				{
					gameListAdapter.setGames(games);
					availableUsersAdapter.setUsers(users);
				});

				break;

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	private void popBackToLoginScreen()
	{
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		for (String tag : new String[] {"gamelist", "create_game", "game"})
		{
			Fragment fragment = fragmentManager.findFragmentByTag(tag);
			if (fragment != null)
				fragmentTransaction.remove(fragment);
		}

		fragmentTransaction.commit();
	}

	@Override
	public void connectionClosed()
	{
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

	public void setEventHandler(EventHandler eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	public GameListAdapter getGameListAdapter()
	{
		return gameListAdapter;
	}

	public AvailableUsersAdapter getAvailableUsersAdapter()
	{
		return availableUsersAdapter;
	}

	private class ConnectAsyncTask extends AsyncTask<Void, Void, Void>
	{
		private ProgressDialog dialog;

		public ConnectAsyncTask()
		{
			dialog = new ProgressDialog(MainActivity.this);
		}

		@Override
		protected void onPreExecute()
		{
			dialog.setMessage("Connecting...");
			dialog.show();
		}

		protected Void doInBackground(Void... args)
		{
			if (connection != null)
				return null;

			AccessToken accessToken = AccessToken.getCurrentAccessToken();
			if (accessToken == null)
				return null;

			final String host = "akos0.ddns.net";
			final int port = 8128;

			try
			{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(host, port), 1000);
				connection = new ProtoConnection(socket);
				actionSender = new ProtoActionSender(connection);
				connection.addMessageHandler(MainActivity.this);
				connection.start();
				connection.sendMessage(MainProto.Message.newBuilder().setLogin(MainProto.Login.newBuilder()
						.setFacebookToken(accessToken.getToken())
						.build())
						.build());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void result)
		{
			if (connection != null)
			{
				GameListFragment gameListFragment = new GameListFragment();

				getFragmentManager().beginTransaction()
						.add(R.id.fragment_container, gameListFragment, "gamelist")
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
						.addToBackStack(null)
						.commit();
			}

			if (dialog.isShowing())
			{
				dialog.dismiss();
			}
		}
	}
}
