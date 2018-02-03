package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.message.proto.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ClientManager implements Runnable
{
	private GameSession currentGame;
	
	private List<PacketHandler> handlers = new ArrayList<>();
	private BlockingQueue<PlayerPacket> packetsReceived = new LinkedBlockingQueue<>();

	private List<Player> players = new ArrayList<>();
	private Object connectionLock = new Object();

	private File pointsDir;

	public ClientManager(int beginnerPlayer, Collection<String> names, File pointsDir)
	{
		if (beginnerPlayer < 0 || beginnerPlayer >= 4 || names.size() != 4)
			throw new IllegalArgumentException();

		for (int i = 0; i < 4; i++)
		{
			final int id = i;
			handlers.add(new PacketHandler()
			{
				public void handlePacket(Packet p)
				{
					packetsReceived.add(new PlayerPacket(id, p));
				}
				
				public void connectionClosed()
				{
					playerConnectionClosed(id);
				}
			});
		}
	}
	
	public void run()
	{
		try
		{
			currentGame = new GameSession(players);
			currentGame.startSession();

			while (!Thread.interrupted())
			{
				PlayerPacket pp = packetsReceived.take();
				if (pp.packet instanceof PacketAction)
				{
					((ProtoPlayer)players.get(pp.player)).processAction(((PacketAction)pp.packet).getAction());
				}
			}
		}
		catch (InterruptedException e)
		{
		}
		close();
	}

	public void playerConnectionClosed(int player)
	{
		System.out.println(players.get(player).getName() + " disconnected.");
		close();
	}

	public void close()
	{
		currentGame.stopSession();
		System.out.println("Stopping game");
	}

	public void loginAuthorized(String name, Connection connection)
	{
		synchronized (connectionLock)
		{
			int playerID = players.size();
			players.add(new ProtoPlayer(playerID, name, connection));
			System.out.println("User logged in: " + name);
			connection.addPacketHandler(handlers.get(playerID));
			if (players.size() == 4)
				new Thread(this).start();
		}
	}

	private static class PlayerPacket
	{
		public final int player;
		public final Packet packet;
		
		public PlayerPacket(int player, Packet packet)
		{
			this.player = player;
			this.packet = packet;
		}
	}
}
