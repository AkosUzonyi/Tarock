package com.tisza.tarock.gui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import androidx.lifecycle.*;
import com.facebook.*;
import com.google.android.gms.auth.api.signin.*;
import com.google.firebase.iid.*;
import com.tisza.tarock.BuildConfig;
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

public class ConnectionViewModel extends AndroidViewModel implements MessageHandler
{
	private SSLSocketFactory socketFactory;
	private ProtoConnection connection;
	private ConnectAsyncTask connectAsyncTask = null;
	private Collection<EventHandler> eventHandlers = new ArrayList<>();
	private Executor uiThreadExecutor = new UIThreadExecutor();
	private ConnectivityManager connectivityManager;

	private MutableLiveData<ConnectionState> connectionState = new MutableLiveData<>(ConnectionState.DISCONNECTED);
	private MutableLiveData<ErrorState> errorState = new MutableLiveData<>(null);
	private MutableLiveData<List<GameInfo>> games = new MutableLiveData<>(new ArrayList<>());
	private MutableLiveData<List<User>> users = new MutableLiveData<>(new ArrayList<>());
	private MutableLiveData<Integer> userID = new MutableLiveData<>(null);

	public ConnectionViewModel(Application application) throws IOException, GeneralSecurityException
	{
		super(application);

		connectivityManager = (ConnectivityManager)application.getSystemService(Context.CONNECTIVITY_SERVICE);

		SSLSessionCache sslSessionCache = new SSLSessionCache(application);
		SSLCertificateSocketFactory sslCertificateSocketFactory = (SSLCertificateSocketFactory)SSLCertificateSocketFactory.getDefault(0, sslSessionCache);

		String trustStoreFile = "truststore.bks";
		KeyStore ks = KeyStore.getInstance("BKS");
		ks.load(application.getAssets().open(trustStoreFile), "000000".toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		sslCertificateSocketFactory.setTrustManagers(tmf.getTrustManagers());
		socketFactory = sslCertificateSocketFactory;
	}

	public LiveData<ConnectionState> getConnectionState()
	{
		return connectionState;
	}

	public LiveData<ErrorState> getErrorState()
	{
		return errorState;
	}

	private void error(ErrorState error)
	{
		errorState.setValue(error);
		errorState.setValue(null);
	}

	public LiveData<List<GameInfo>> getGames()
	{
		return games;
	}

	public LiveData<List<User>> getUsers()
	{
		return users;
	}

	public LiveData<Integer> getUserID()
	{
		return userID;
	}

	public void addEventHandler(EventHandler eventHandler)
	{
		eventHandlers.add(eventHandler);
	}

	public void removeEventHandler(EventHandler eventHandler)
	{
		eventHandlers.remove(eventHandler);
	}

	public void login()
	{
		switch (connectionState.getValue())
		{
			case DISCONNECTED:
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
					error(ErrorState.NO_NETWORK);
				else
					new ConnectAsyncTask().execute();
				break;
			case CONNECTING:
			case LOGGING_IN:
				break;
			case LOGGED_IN:
				connectionState.setValue(ConnectionState.LOGGED_IN);
				break;
			case CONNECTED:
				connectionState.setValue(ConnectionState.LOGGING_IN);
				AccessToken facebookToken = AccessToken.getCurrentAccessToken();
				GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getApplication());
				MainProto.Login.Builder loginMessageBuilder = MainProto.Login.newBuilder();
				if (facebookToken != null)
					loginMessageBuilder.setFacebookToken(facebookToken.getToken());
				if (googleAccount != null)
					loginMessageBuilder.setGoogleToken(googleAccount.getIdToken());
				connection.sendMessage(MainProto.Message.newBuilder().setLogin(loginMessageBuilder).build());
				break;
		}
	}

	public void disconnect()
	{
		switch (connectionState.getValue())
		{
			case DISCONNECTED:
				break;
			case CONNECTING:
				connectAsyncTask.cancel(true);
				break;
			case LOGGING_IN:
			case LOGGED_IN:
			case CONNECTED:
				new DisconnectAsyncTask().execute(connection);
				break;
		}
	}

	public void sendMessage(MainProto.Message message)
	{
		if (connectionState.getValue() == ConnectionState.LOGGED_IN)
			connection.sendMessage(message);
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		switch (message.getMessageTypeCase())
		{
			case LOGIN_RESULT:
				boolean loggedIn = message.getLoginResult().hasUserId();

				if (connectionState.getValue() == ConnectionState.LOGGING_IN && !loggedIn)
					error(ErrorState.LOGIN_UNSUCCESSFUL);

				connectionState.setValue(loggedIn ? ConnectionState.LOGGED_IN : ConnectionState.CONNECTED);
				userID.setValue(loggedIn ? message.getLoginResult().getUserId() : null);

				if (loggedIn)
					FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult ->
					{
						sendMessage(MainProto.Message.newBuilder().setFcmToken(MainProto.FCMToken.newBuilder()
								.setFcmToken(instanceIdResult.getToken())
								.setActive(true)
								.build()).build());
					});

				break;

			case EVENT:
				for (EventHandler eventHandler : eventHandlers)
					new ProtoEvent(message.getEvent()).handle(eventHandler);

				break;

			case SERVER_STATUS:

				users.getValue().clear();
				for (MainProto.User userProto : message.getServerStatus().getAvailableUserList())
				{
					users.getValue().add(Utils.userFromProto(userProto));
				}
				users.setValue(users.getValue());

				games.getValue().clear();
				for (MainProto.GameSession gameProto : message.getServerStatus().getAvailableGameSessionList())
				{
					List<User> gameUserList = new ArrayList<>();
					for (int userID : gameProto.getUserIdList())
						for (User user : users.getValue())
							if (user.getId() == userID)
								gameUserList.add(user);

					games.getValue().add(new GameInfo(gameProto.getId(), GameType.fromID(gameProto.getType()), gameUserList, Utils.gameSessionStateFromProto(gameProto.getState())));
				}
				games.setValue(games.getValue());

				break;

			default:
				System.err.println("unhandled message type: " + message.getMessageTypeCase());
				break;
		}
	}

	@Override
	public void connectionError(MessageHandler.ErrorType errorType)
	{
		switch (errorType)
		{
			case INVALID_HELLO:
				error(ErrorState.SERVER_ERROR);
				break;
			case VERSION_MISMATCH:
				error(ErrorState.OUTDATED);
				break;
		}

		connectionState.setValue(ConnectionState.DISCONNECTED);
		connection = null;
	}

	@Override
	public void connectionClosed()
	{
		connection = null;
		connectionState.setValue(ConnectionState.DISCONNECTED);
	}

	@Override
	protected void onCleared()
	{
		disconnect();
	}

	private static class DisconnectAsyncTask extends AsyncTask<ProtoConnection, Void, Void>
	{
		@Override
		protected Void doInBackground(ProtoConnection... protoConnections)
		{
			for (ProtoConnection connection : protoConnections)
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
				}
			}

			return null;
		}
	}

	private class ConnectAsyncTask extends AsyncTask<Void, Void, ProtoConnection>
	{
		@Override
		protected void onPreExecute()
		{
			connectAsyncTask = this;
			connectionState.setValue(ConnectionState.CONNECTING);
		}

		protected ProtoConnection doInBackground(Void... voids)
		{
			try
			{
				Socket socket = socketFactory.createSocket();
				socket.connect(new InetSocketAddress(BuildConfig.SERVER_HOSTNAME, BuildConfig.SERVER_PORT), 1000);
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
			connectAsyncTask = null;

			if (resultProtoConnection == null)
			{
				error(ErrorState.SERVER_DOWN);
				connectionState.setValue(ConnectionState.DISCONNECTED);
				return;
			}

			connection = resultProtoConnection;
			connection.addMessageHandler(ConnectionViewModel.this);
			connection.start();
			connectionState.setValue(ConnectionState.CONNECTED);
			login();
		}

		@Override
		protected void onCancelled(ProtoConnection resultProtoConnection)
		{
			new DisconnectAsyncTask().execute(resultProtoConnection);
			connectAsyncTask = null;
			connectionState.setValue(ConnectionState.DISCONNECTED);
		}
	}

	public static enum ConnectionState
	{
		DISCONNECTED, CONNECTING, CONNECTED, LOGGING_IN, LOGGED_IN;
	}

	public static enum ErrorState
	{
		OUTDATED, NO_NETWORK, SERVER_DOWN, SERVER_ERROR, LOGIN_UNSUCCESSFUL;
	}
}
