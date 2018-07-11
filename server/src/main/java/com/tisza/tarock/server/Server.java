package com.tisza.tarock.server;

import com.tisza.tarock.net.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server implements Runnable
{
	private final int port;
	private Thread listenerThread;
	private ServerSocket ss;

	private ExecutorService gameExecutorService = Executors.newSingleThreadExecutor(new GameThreadFactory());

	private List<Client> clients = new ArrayList<>();

	private final GameSessionManager gameSessionManager = new GameSessionManager();
	private final FacebookUserManager facebookUserManager = new FacebookUserManager();

	public Server(int port)
	{
		this.port = port;
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

	@Override
	public void run()
	{
		try
		{
			ss = new ServerSocket(port);
			System.out.println("Server listening on: " + ss.getLocalSocketAddress());
			while (!Thread.interrupted())
			{
				Socket socket = ss.accept();
				ProtoConnection connection = new ProtoConnection(socket, gameExecutorService);
				clients.add(new Client(this, connection));
				connection.start();
			}
		}
		catch (IOException e)
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
		if (ss != null)
		{
			try
			{
				ss.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				ss = null;
			}
		}
	}
}
