package com.tisza.tarock.server;

import com.tisza.tarock.net.Connection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Server
{
	private int port;
	private List<String> playerNames;
	private File pointsDir;
	
	private LoginManager loginManager;
	private ClientManager gameSession;
	private Thread listenterThread = null;
	private ServerSocket ss;

	public static void main(String[] args)
	{
		new Server(8128, Arrays.asList("a", "b", "c", "d"), null).start();
	}

	public Server(int port, List<String> playerNames, File pointsDir)
	{
		this.port = port;
		this.playerNames = playerNames;
		this.pointsDir = pointsDir;
	}
	
	public void start()
	{
		if (listenterThread == null)
		{
			gameSession = new ClientManager(0, playerNames, pointsDir);
			loginManager = new LoginManager(gameSession);
			listenterThread = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						ss = new ServerSocket(port);
						System.out.println(ss.getLocalSocketAddress());
						while (!Thread.interrupted())
						{
							Socket s = ss.accept();
							System.out.println(s.getRemoteSocketAddress());
							Connection c = new Connection(s);
							//c.start();
							loginManager.newConnection(c);
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					finally
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
						}
					}
				}
			});
			listenterThread.start();
		}
	}
	
	public void stop()
	{
		if (listenterThread != null)
		{
			listenterThread.interrupt();
			try
			{
				ss.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			listenterThread = null;
			loginManager.close();
			gameSession.close();
		}
	}
}
