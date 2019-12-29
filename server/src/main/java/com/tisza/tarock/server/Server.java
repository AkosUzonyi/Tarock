package com.tisza.tarock.server;

import com.tisza.tarock.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.net.*;
import io.reactivex.*;
import io.reactivex.disposables.*;
import org.apache.log4j.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;

public class Server implements Runnable
{
	private static final Logger log = Logger.getLogger(Server.class);

	private final int port;
	private Thread listenerThread;
	private SSLServerSocket serverSocket;

	private Collection<Client> clients = new ArrayList<>();

	private final TarockDatabase database;
	private final GameSessionManager gameSessionManager;
	private final FacebookUserManager facebookUserManager;
	private final GoogleUserManager googleUserManager;
	private final FirebaseNotificationSender firebaseNotificationSender;

	private CompositeDisposable disposables = new CompositeDisposable();

	public Server(int port)
	{
		this.port = port;

		database = new TarockDatabase();
		gameSessionManager = new GameSessionManager(database);
		facebookUserManager = new FacebookUserManager(database);
		googleUserManager = new GoogleUserManager(database);
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

	public GoogleUserManager getGoogleUserManager()
	{
		return googleUserManager;
	}

	public FirebaseNotificationSender getFirebaseNotificationSender()
	{
		return firebaseNotificationSender;
	}

	public boolean isUserLoggedIn(User user)
	{
		return user.isBot() || clients.stream().map(Client::getLoggedInUser).anyMatch(u -> u == user);
	}

	public void removeClient(Client client)
	{
		if (clients.remove(client))
		{
			client.disconnect();
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

		log.info("Server listening on: " + serverSocket.getLocalSocketAddress());
	}

	@Override
	public void run()
	{
		try
		{
			database.initialize();
			gameSessionManager.initialize();
			facebookUserManager.refreshImageURLs();

			createServerSocket();

			while (!Thread.interrupted())
			{
				SSLSocket socket = (SSLSocket)serverSocket.accept();
				handleNewSocket(socket);
			}
		}
		catch (Exception e)
		{
			log.error("Server exception", e);
		}
		finally
		{
			disposables.dispose();
			closeSocket();

			for (Client client : clients)
			{
				removeClient(client);
			}

			database.shutdown();
			gameSessionManager.shutdown();
			listenerThread = null;

			log.info("Server stopped");
		}
	}

	private void handleNewSocket(SSLSocket socket)
	{
		try
		{
			log.info("Accepted connection from: " + socket.getRemoteSocketAddress());
			ProtoConnection connection = new ProtoConnection(socket, Main.GAME_EXECUTOR_SERVICE);
			clients.add(new Client(this, connection));
			connection.start();
		}
		catch (Exception e)
		{
			log.error("Exception while adding new client", e);
		}
	}

	public void broadcastStatus()
	{
		for (Client client : clients)
		{
			if (client.getLoggedInUser() == null)
				continue;

			MainProto.ServerStatus.Builder builder = MainProto.ServerStatus.newBuilder();

			for (GameSession gameSession : gameSessionManager.getGameSessions())
			{
				MainProto.Game.Builder gameBuilder = MainProto.Game.newBuilder()
						.setId(gameSession.getID())
						.setType(gameSession.getGameType().getID())
						.addAllPlayerName(gameSession.getPlayerNames())
						.setMy(gameSession.isUserPlaying(client.getLoggedInUser()));
				builder.addAvailableGame(gameBuilder);
			}

			disposables.add(
			database.getUsers().flatMapCompletable(user ->
			client.getLoggedInUser().isFriendWith(user).flatMapCompletable(isFriend ->
			Utils.userToProto(user, isFriend, isUserLoggedIn(user)).flatMapCompletable(userProto ->
			{
				if (!user.equals(client.getLoggedInUser()))
					builder.addAvailableUser(userProto);
				return Completable.complete();
			})))
			.subscribe(() -> client.sendMessage(MainProto.Message.newBuilder().setServerStatus(builder.build()).build())));
		}
	}

	public void start()
	{
		if (listenerThread == null)
		{
			listenerThread = new Thread(this, "ListenerThread");
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

	public void awaitTermination(long millis) throws InterruptedException
	{
		if (listenerThread != null)
		{
			listenerThread.join(millis);
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
				log.warn("Exception while closing server socket: " + e.getMessage());
			}
			finally
			{
				serverSocket = null;
			}
		}
	}
}
