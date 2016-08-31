package com.tisza.tarock.server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.tisza.tarock.net.*;

public class Server
{
	private int port;
	private List<String> playerNames;
	
	private LoginManager loginManager;
	private GameSession gameSession;
	private Thread listenterThread = null;
	
	public Server(int port, List<String> playerNames)
	{
		this.port = port;
		this.playerNames = playerNames;
	}
	
	public void start()
	{
		if (listenterThread == null)
		{
			gameSession = new GameSession(0, playerNames);
			loginManager = new LoginManager(gameSession);
			listenterThread = new Thread(new Runnable()
			{
				public void run()
				{
					ServerSocket ss = null;
					try
					{
						ss = new ServerSocket(8128);
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
			listenterThread = null;
			loginManager.close();
			gameSession.close();
		}
	}
}
