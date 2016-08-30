package com.tisza.tarock.server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.tisza.tarock.net.*;

public class Server
{
	private int port;
	private List<String> playerNames;
	
	private GameSession gameSession;
	private Thread listenterThread = null;
	private List<Connection> openedConnections = new ArrayList<Connection>();
	
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
			listenterThread = new Thread(new Runnable()
			{
				public void run()
				{
					ServerSocket ss = null;
					try
					{
						ss = new ServerSocket(port);
						while (!Thread.interrupted())
						{
							Socket s = ss.accept();
							Connection c = new Connection(s);
							openedConnections.add(c);
							c.addPacketHandler(new LoginHandler(gameSession, c));
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
			for (Connection c : openedConnections)
			{
				c.closeRequest();
			}
			listenterThread.interrupt();
			listenterThread = null;
			gameSession.close();
		}
	}
}
