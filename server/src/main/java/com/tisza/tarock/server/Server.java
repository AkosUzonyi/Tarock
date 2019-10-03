package com.tisza.tarock.server;

import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public class Server implements Runnable
{
	private final int port;
	private Thread listenerThread;
	private SSLServerSocket serverSocket;

	private final File keystoreFile;

	private final ScheduledExecutorService gameExecutorService;

	private List<Client> clients = new ArrayList<>();

	private final GameSessionManager gameSessionManager;
	private final FacebookUserManager facebookUserManager;
	private final FirebaseNotificationSender firebaseNotificationSender;

	public Server(int port, File staticDir, File dataDir)
	{
		this.port = port;
		this.keystoreFile = new File(staticDir, "keystore");

		gameExecutorService = new GameExecutorService();
		gameSessionManager = new GameSessionManager(dataDir, new RandomPlayerFactory());
		facebookUserManager = new FacebookUserManager(dataDir);
		firebaseNotificationSender = new FirebaseNotificationSender(new File(staticDir, "fcm-service-account.json"));
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
		ks.load(new FileInputStream(keystoreFile), "000000".toCharArray());

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
			facebookUserManager.initialize();

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

			gameSessionManager.shutdown();
			facebookUserManager.shutdown();
			gameExecutorService.shutdownNow();

			System.out.println("server stopped");
		}
	}

	private void handleNewSocket(SSLSocket socket)
	{
		try
		{
			System.out.println("accepted connection from: " + socket.getRemoteSocketAddress());
			ProtoConnection connection = new ProtoConnection(socket, gameExecutorService);
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

			for (User user : facebookUserManager.listUsers())
			{
				if (!user.equals(client.getLoggedInUser()))
					builder.addAvailableUser(Utils.userToProto(user, client.getLoggedInUser().isFriendWith(user)));
			}

			client.sendMessage(MainProto.Message.newBuilder().setServerStatus(builder.build()).build());
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

	private class RandomPlayerFactory implements BotFactory
	{
		@Override
		public Player createBot(int n)
		{
			return new RandomPlayer("bot" + n, gameExecutorService, 500, 2000);
		}
	}
}
