package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.player.*;
import com.tisza.tarock.player.proto.*;
import com.tisza.tarock.player.random.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable
{
	private int port;

	private GameSession gameSession = null;
	private Thread listenterThread = null;
	private ServerSocket ss;

	public static void main(String[] args)
	{
		new Server(8128).start();
	}

	public Server(int port)
	{
		this.port = port;

		//joinedPlayers.add(new RandomPlayer("bot0"));
		//joinedPlayers.add(new RandomPlayer("bot1"));
		//joinedPlayers.add(new RandomPlayer("bot2"));
	}

	//TODO: move
	List<Player> joinedPlayers = new ArrayList<>();

	private void listenLoop() throws IOException
	{
		while (!Thread.interrupted())
		{
			Socket s = ss.accept();
			System.out.println(s.getRemoteSocketAddress());
			ProtoConnection connection = new ProtoConnection(s);
			ProtoPlayer player = new ProtoPlayer(connection);
			player.setJoinRequestHandler(gameID ->
			{
				joinedPlayers.add(player);
				if (joinedPlayers.size() == 4)
				{
					gameSession = new GameSession(joinedPlayers);
					gameSession.startSession();
				}
			});
			connection.start();
		}
	}

	public void run()
	{
		try
		{
			ss = new ServerSocket(port);
			System.out.println(ss.getLocalSocketAddress());
			listenLoop();
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
			if (gameSession != null)
			{
				gameSession.stopSession();
			}
			listenterThread = null;
		}
	}

	public void start()
	{
		if (listenterThread == null)
		{
			listenterThread = new Thread(this);
			listenterThread.start();
		}
	}
	
	public void stop()
	{
		if (listenterThread != null)
		{
			listenterThread.interrupt();
			listenterThread = null;
		}
	}
}
