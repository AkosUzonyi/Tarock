package com.tisza.tarock.server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.tisza.tarock.net.*;

public class Server
{
	private int port;
	private List<String> playerNames;
	private File pointsDir;
	
	private LoginManager loginManager;
	private GameSession gameSession;
	private Thread listenterThread = null;
	private ServerSocket ss;
	
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
			gameSession = new GameSession(0, playerNames, pointsDir);
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
