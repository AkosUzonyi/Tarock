package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.player.*;
import com.tisza.tarock.player.proto.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server implements Runnable
{
	private static final int PROTO_PLAYER_COUNT = 1;
	private static final PhaseEnum FAST_FORWARD_TO_PHASE = null;
	private static final int RANDOM_PLAYER_DELAY = 500, RANDOM_PLAYER_EXTRA_DELAY = 2500;

	private int port;

	private GameSession gameSession;
	private Thread listenerThread;
	private ServerSocket ss;

	private List<ProtoPlayer> protoPlayers = new ArrayList<>();

	public static void main(String[] args) throws IOException
	{
		Server server = new Server(8128);
		server.start();
		System.in.read();
		server.stop();
	}

	public Server(int port)
	{
		this.port = port;

		List<Player> players = new ArrayList<>();
		for (int i = 0; i < 4; i++)
		{
			Player player;

			if (i < PROTO_PLAYER_COUNT)
			{
				ProtoPlayer protoPlayer = new ProtoPlayer("proto" + i);
				protoPlayers.add(protoPlayer);
				player = protoPlayer;
			}
			else
			{
				player = new RandomPlayer("bot" + i, RANDOM_PLAYER_DELAY, RANDOM_PLAYER_EXTRA_DELAY);
			}

			if (FAST_FORWARD_TO_PHASE != null)
				player = new MixedPlayer(new RandomPlayer(player.getName()), player, FAST_FORWARD_TO_PHASE);

			players.add(player);
		}
		gameSession = new GameSession(GameType.ZEBI, players);
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
		catch (SocketException e) {}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			closeSocket();

			if (gameSession != null)
			{
				gameSession.stopSession();
				gameSession = null;
			}
			listenerThread = null;
			System.out.println("server stopped");
		}
	}

	public void start()
	{
		gameSession.startSession();

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
