package com.tisza.tarock.server;

import com.tisza.tarock.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.net.*;
import com.tisza.tarock.server.player.*;
import io.reactivex.*;
import io.reactivex.disposables.*;
import org.apache.log4j.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class Server implements Runnable
{
	private static final Logger log = Logger.getLogger(Server.class);

	private final int port;
	private Thread listenerThread;
	private SSLServerSocket serverSocket;

	private final Collection<Client> clients = Collections.synchronizedList(new ArrayList<>());

	private final TarockDatabase database;
	private final GameSessionManager gameSessionManager;
	private final FacebookUserManager facebookUserManager;
	private final GoogleUserManager googleUserManager;
	private final FirebaseNotificationSender firebaseNotificationSender;

	public Server(int port)
	{
		this.port = port;

		database = new TarockDatabase();
		gameSessionManager = new GameSessionManager(this);
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
		if (user.isBot())
			return true;

		synchronized (clients)
		{
			for (Client client : clients)
				if (user.equals(client.getLoggedInUser()))
					return true;
		}

		return false;
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
			firebaseNotificationSender.initialize();
			gameSessionManager.initialize();
			facebookUserManager.refreshImageURLs();

			createServerSocket();

			Main.GAME_EXECUTOR_SERVICE.scheduleAtFixedRate(this::hourlyTask, 0, 1, TimeUnit.HOURS);

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

	private void hourlyTask()
	{
		gameSessionManager.deleteOldGames();
		broadcastStatus();
	}

	public void broadcastStatus()
	{
		MainProto.ServerStatus.Builder builder = MainProto.ServerStatus.newBuilder();

		for (GameSession gameSession : gameSessionManager.getGameSessions())
		{
			if (gameSession.getState() == GameSession.State.ENDED)
				continue;

			MainProto.GameSession.Builder gameBuilder = MainProto.GameSession.newBuilder()
					.setId(gameSession.getID())
					.setType(gameSession.getGameType().getID())
					.addAllUserId(gameSession.getPlayers().stream().map(p -> p.getUser().getID()).collect(Collectors.toList()))
					.setState(Utils.gameSessionStateToProto(gameSession.getState()));

			builder.addAvailableGameSession(gameBuilder);
		}

		database.getUsers().flatMapCompletable(user ->
		Utils.userToProto(user, false, isUserLoggedIn(user)).flatMapCompletable(userProto ->
		{
			builder.addAvailableUser(userProto);
			return Completable.complete();
		}))
		.subscribe(() ->
		{
			synchronized (clients)
			{
				for (Client client : clients)
				{
					if (client.getLoggedInUser() == null)
						continue;

					client.sendMessage(MainProto.Message.newBuilder().setServerStatus(builder.build()).build());
				}
			}
		});
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
