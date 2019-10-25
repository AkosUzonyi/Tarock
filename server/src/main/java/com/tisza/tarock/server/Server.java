package com.tisza.tarock.server;

import com.tisza.tarock.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;
import io.reactivex.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;

public class Server implements Runnable
{

	private final int port;
	private Thread listenerThread;
	private SSLServerSocket serverSocket;

	private Collection<Client> clients = new ArrayList<>();
	private Collection<User> loggedInUsers = new HashSet<>();

	private final TarockDatabase database;
	private final GameSessionManager gameSessionManager;
	private final FacebookUserManager facebookUserManager;
	private final FirebaseNotificationSender firebaseNotificationSender;

	public Server(int port)
	{
		this.port = port;

		database = new TarockDatabase();
		gameSessionManager = new GameSessionManager(database);
		facebookUserManager = new FacebookUserManager(database);
		firebaseNotificationSender = new FirebaseNotificationSender(new File(Main.STATIC_DIR, "fcm-service-account.json"));
	}

	public TarockDatabase getDatabase()
	{
		return database;
	}

	public GameSessionManager getGameSessionManager()
	{
		return gameSessionManager;
	}

	public FacebookUserManager getFacebookUserManager()
	{
		return facebookUserManager;
	}

	public FirebaseNotificationSender getFirebaseNotificationSender()
	{
		return firebaseNotificationSender;
	}

	public void loginUser(User user)
	{
		loggedInUsers.add(user);
		user.getName().subscribe(name -> System.out.println("user logged in: " + name + " (id: " + user.getID() + ")"));
	}

	public void logoutUser(User user)
	{
		loggedInUsers.remove(user);
		user.getName().subscribe(name -> System.out.println("user logged out: " + name + " (id: " + user.getID() + ")"));
	}

	public boolean isUserLoggedIn(User user)
	{
		return loggedInUsers.contains(user);
	}

	public void removeClient(Client client)
	{
		if (clients.remove(client))
		{
			client.disonnect();
		}
		broadcastStatus();
	}

	private void createServerSocket() throws IOException, GeneralSecurityException
	{
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(new File(Main.STATIC_DIR, "keystore")), "000000".toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, "000000".toCharArray());

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

		SSLServerSocketFactory ssf = sc.getServerSocketFactory();
		serverSocket = (SSLServerSocket)ssf.createServerSocket(port);

		System.out.println("Server listening on: " + serverSocket.getLocalSocketAddress());
	}

	@Override
	public void run()
	{
		try
		{
			database.initialize();

			createServerSocket();

			while (!Thread.interrupted())
			{
				SSLSocket socket = (SSLSocket)serverSocket.accept();
				handleNewSocket(socket);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeSocket();

			for (Client client : clients)
			{
				removeClient(client);
			}

			database.shutdown();
			gameSessionManager.shutdown();

			System.out.println("server stopped");
		}
	}

	private void handleNewSocket(SSLSocket socket)
	{
		try
		{
			System.out.println("accepted connection from: " + socket.getRemoteSocketAddress());
			ProtoConnection connection = new ProtoConnection(socket, Main.GAME_EXECUTOR_SERVICE);
			clients.add(new Client(this, connection));
			connection.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void broadcastStatus()
	{
		for (Client client : clients)
		{
			if (client.getLoggedInUser() == null)
				continue;

			MainProto.ServerStatus.Builder builder = MainProto.ServerStatus.newBuilder();
			for (GameInfo gameInfo : gameSessionManager.listGames())
			{
				builder.addAvailableGame(Utils.gameInfoToProto(gameInfo, gameSessionManager.isGameOwnedBy(gameInfo.getId(), client.getLoggedInUser())));
			}

			database.getUsers().flatMapCompletable(user ->
			client.getLoggedInUser().isFriendWith(user).flatMapCompletable(isFriend ->
			Utils.userToProto(user, isFriend, isUserLoggedIn(user)).flatMapCompletable(userProto ->
			{
				if (!user.equals(client.getLoggedInUser()))
					builder.addAvailableUser(userProto);
				return Completable.complete();
			})))
			.subscribe(() -> client.sendMessage(MainProto.Message.newBuilder().setServerStatus(builder.build()).build()));
		}
	}

	public void start()
	{
		if (listenerThread == null)
		{
			listenerThread = new Thread(this);
			listenerThread.start();
		}
	}

	public void stop()
	{
		closeSocket();
		if (listenerThread != null)
		{
			listenerThread.interrupt();
		}
	}

	private void closeSocket()
	{
		if (serverSocket != null)
		{
			try
			{
				serverSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				serverSocket = null;
			}
		}
	}
}
