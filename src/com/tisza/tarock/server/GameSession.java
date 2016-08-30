package com.tisza.tarock.server;

import java.util.*;

import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.gamephase.*;

public class GameSession
{
	private int nextBeginnerPlayer;
	
	private GamePhase currentGamePhase;
	private GameInstance currentGame;
	
	//sorted by id
	private List<String> playerNames = new ArrayList<String>();
	private List<PacketHandler> handlers = new ArrayList<PacketHandler>();
	
	private Map<Integer, Connection> playerIDToConnection = new HashMap<Integer, Connection>();
	
	public GameSession(int beginnerPlayer, Collection<String> names)
	{
		if (beginnerPlayer < 0 || beginnerPlayer >= 4 || names.size() != 4)
			throw new IllegalArgumentException();
		
		nextBeginnerPlayer = beginnerPlayer;
		
		playerNames = new ArrayList<String>(names);
		Collections.shuffle(playerNames);
		
		for (int i = 0; i < 4; i++)
		{
			final int id = i;
			handlers.add(new PacketHandler()
			{
				public void handlePacket(Packet p)
				{
					packetFromPlayer(id, p);
				}
				
				public void connectionClosed()
				{
					playerConnectionClosed(id);
				}
			});
		}
	}
	
	public void startNewGame(boolean doubleRound)
	{
		if (playerIDToConnection.size() != 4)
			throw new IllegalStateException();
		
		if (doubleRound) nextBeginnerPlayer--;
		currentGame = new GameInstance(nextBeginnerPlayer);
		nextBeginnerPlayer = (nextBeginnerPlayer + 1) % 4;
		
		for (int i = 0; i < 4; i++)
		{
			sendPacketToPlayer(i, new PacketStartGame(playerNames, i));
		}
		changeGamePhase(new PhaseDealing(this));
	}
		
	public GameInstance getCurrentGame()
	{
		return currentGame;
	}

	public void changeGamePhase(GamePhase newPhase)
	{
		if (currentGame == null)
			throw new IllegalStateException();
		currentGamePhase = newPhase;
		currentGamePhase.start();
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (currentGamePhase != null)
		{
			currentGamePhase.packetFromPlayer(player, packet);
		}
	}

	public void playerConnectionClosed(int player)
	{
		disconnectFromPlayer(player);
	}
	
	public void sendPacketToPlayer(int player, Packet packet)
	{
		if (playerIDToConnection.containsKey(player))
		{
			playerIDToConnection.get(player).sendPacket(packet);
		}
	}
	
	public void broadcastPacket(Packet packet)
	{
		for (Connection c : playerIDToConnection.values())
		{
			c.sendPacket(packet);
		}
	}
	
	public void close()
	{
		for (int i = 0; i < 4; i++)
		{
			disconnectFromPlayer(i);
		}
		currentGame = null;
	}
	
	public synchronized void loginAuthorized(String name, Connection connection)
	{
		String failure = null;
		
		if (!playerNames.contains(name))
		{
			failure = "You are not part of this game";
		}
		else
		{
			final int player = playerNames.indexOf(name);
			if (playerIDToConnection.containsKey(player))
			{
				failure = "User already logged in";
			}
			else
			{
				playerIDToConnection.put(player, connection);
				connection.addPacketHandler(handlers.get(player));
			}
		}
		
		if (failure != null)
		{
			connection.sendPacket(new PacketLoginFailed(failure));
			connection.closeRequest();
		}
	}

	public synchronized void logout(String name)
	{
		disconnectFromPlayer(playerNames.indexOf(name));
	}
	
	private synchronized void disconnectFromPlayer(int player)
	{
		if (playerIDToConnection.containsKey(player))
		{
			Connection c = playerIDToConnection.remove(player);
			c.removePacketHandler(handlers.get(player));
			c.closeRequest();
		}
	}
}
