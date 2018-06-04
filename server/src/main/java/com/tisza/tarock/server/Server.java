package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.player.*;
import com.tisza.tarock.player.proto.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable
{
	private int port;

	private GameSession gameSession = null;
	private Thread listenterThread = null;
	private ServerSocket ss;

	private List<ProtoPlayer> protoPlayers = new ArrayList<>();

	public static void main(String[] args)
	{
		new Server(8128, 3).start();
	}

	public Server(int port, int randomPlayerCount)
	{
		if (randomPlayerCount < 0 || randomPlayerCount >= 4)
			throw new IllegalArgumentException();

		this.port = port;

		List<Player> players = new ArrayList<>();
		for (int i = 0; i < 4; i++)
		{
			if (i < randomPlayerCount)
			{
				players.add(new RandomPlayer("bot" + i, 500));
			}
			else
			{
				ProtoPlayer protoPlayer = new ProtoPlayer("proto" + i);
				protoPlayers.add(protoPlayer);
				players.add(protoPlayer);
				//players.add(new MixedPlayer(new RandomPlayer(protoPlayer.getName(), 500), protoPlayer, PhaseEnum.ANNOUNCING));
			}
		}
		gameSession = new GameSession(GameType.ZEBI, players);
		gameSession.startSession();
	}

	private void newConnection(Socket s) throws IOException
	{
		System.out.println(s.getRemoteSocketAddress());

		for (ProtoPlayer player : protoPlayers)
		{
			if (!player.isConnected())
			{
				ProtoConnection connection = new ProtoConnection(s);
				connection.start();
				player.useConnection(connection);
				return;
			}
		}
		s.close();
	}

	@Override
	public void run()
	{
		try
		{
			ss = new ServerSocket(port);
			System.out.println(ss.getLocalSocketAddress());
			while (!Thread.interrupted())
			{
				Socket s = ss.accept();
				newConnection(s);
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
			if (gameSession != null)
			{
				gameSession.stopSession();
			}
			listenterThread = null;
		}
		System.out.println("server stopped");
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
