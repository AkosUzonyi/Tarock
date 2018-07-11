package com.tisza.tarock.server;

import com.tisza.tarock.net.*;

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

	private ExecutorService gameExecutorService = Executors.newSingleThreadExecutor(new GameThreadFactory());

	private List<Client> clients = new ArrayList<>();

	private final GameSessionManager gameSessionManager = new GameSessionManager();
	private final FacebookUserManager facebookUserManager = new FacebookUserManager();

	public Server(int port, File keystoreFile)
	{
		this.port = port;
		this.keystoreFile = keystoreFile;
	}

	public GameSessionManager getGameSessionManager()
	{
		return gameSessionManager;
	}

	public FacebookUserManager getFacebookUserManager()
	{
		return facebookUserManager;
	}

	public void removeClient(Client client)
	{
		if (clients.remove(client))
		{
			client.disonnect();
		}
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
			createServerSocket();

			while (!Thread.interrupted())
			{
				SSLSocket socket = (SSLSocket)serverSocket.accept();
				ProtoConnection connection = new ProtoConnection(socket, gameExecutorService);
				clients.add(new Client(this, connection));
				connection.start();
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

	public void broadcastStatus()
	{
		for (Client client : clients)
		{
			client.sendStatus();
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
