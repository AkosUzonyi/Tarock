package com.tisza.tarock.server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.tisza.tarock.game.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.gamephase.*;

public class GameSession implements Runnable
{
	private int nextBeginnerPlayer;
	
	private GamePhase currentGamePhase;
	private GameInstance currentGame;
	
	//sorted by id
	private List<String> playerNames = new ArrayList<String>();
	private List<PacketHandler> handlers = new ArrayList<PacketHandler>();
	
	private Map<Integer, Connection> playerIDToConnection = new HashMap<Integer, Connection>();
	private Object connectionLock = new Object();
	
	private BlockingQueue<PlayerPacket> packetsReceived = new LinkedBlockingQueue<PlayerPacket>();
	
	private Points points;
	private File pointsFile;
	
	private Thread gameThread;
	
	public GameSession(int beginnerPlayer, Collection<String> names, File pf)
	{
		if (beginnerPlayer < 0 || beginnerPlayer >= 4 || names.size() != 4)
			throw new IllegalArgumentException();
		
		playerNames = new ArrayList<String>(names);
		Collections.shuffle(playerNames);
		
		points = new Points(playerNames);
		pointsFile = pf;
		
		nextBeginnerPlayer = beginnerPlayer;
		
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
		
		gameThread = new Thread(this, "Game thread");
		gameThread.start();
	}
	
	public void startNewGame(boolean doubleRound)
	{
		if (Thread.currentThread() != gameThread)
			throw new RuntimeException();
		
		System.out.println("start game");
		
		if (doubleRound) nextBeginnerPlayer--;
		currentGame = new GameInstance(nextBeginnerPlayer);
		nextBeginnerPlayer = (nextBeginnerPlayer + 1) % 4;
		
		for (int i = 0; i < 4; i++)
		{
			sendPacketToPlayer(i, new PacketStartGame(playerNames, i));
		}
		changeGamePhase(new PhaseDealing(this));
	}
	
	public void run()
	{
		if (pointsFile != null)
		{
			try
			{
				pointsFile.createNewFile();
				FileInputStream fis = new FileInputStream(pointsFile);
				points.readData(fis);
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			waitForAllPlayersToConnect();
			
			startNewGame(false);
			
			while (!Thread.interrupted())
			{
				PlayerPacket pp = packetsReceived.take();
				waitForAllPlayersToConnect();
				currentGamePhase.packetFromPlayer(pp.player, pp.packet);
			}
		}
		catch (InterruptedException e)
		{
			return;
		}
	}
	
	private void waitForAllPlayersToConnect() throws InterruptedException
	{
		while (playerIDToConnection.size() != 4)
		{
			synchronized (connectionLock)
			{
				connectionLock.wait();
			}
		}
	}
	
	private void checkThread()
	{
		if (Thread.currentThread() != gameThread)
			throw new RuntimeException();
	}

	public Points getPoints()
	{
		return points;
	}
	
	public void savePoints()
	{
		checkThread();
		
		if (pointsFile != null)
		{
			try
			{
				FileOutputStream fos = new FileOutputStream(pointsFile, true);
				points.appendDirtyData(fos);
				fos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
		
	public GameInstance getCurrentGame()
	{
		return currentGame;
	}

	public void changeGamePhase(GamePhase newPhase)
	{
		checkThread();
		
		if (currentGame == null)
			throw new IllegalStateException();
		currentGamePhase = newPhase;
		currentGamePhase.start();
	}

	public void playerConnectionClosed(int player)
	{
		disconnectFromPlayer(player);
	}
	
	public void sendPacketToPlayer(int player, Packet packet)
	{
		checkThread();
		
		if (playerIDToConnection.containsKey(player))
		{
			playerIDToConnection.get(player).sendPacket(packet);
		}
	}
	
	public void broadcastPacket(Packet packet)
	{
		checkThread();
		
		for (Connection c : playerIDToConnection.values())
		{
			c.sendPacket(packet);
		}
	}
	
	public void close()
	{
		gameThread.interrupt();
		try
		{
			gameThread.join(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		currentGame = null;
		for (int i = 0; i < 4; i++)
		{
			disconnectFromPlayer(i);
		}
	}
	
	private void onSuccessfulLogin(int player)
	{
		if (currentGamePhase != null)
		{
			//currentGamePhase.playerLoggedIn(player);
		}
	}
	
	public void loginAuthorized(String name, Connection connection)
	{
		PacketLoginFailed.Reason failureReason = null;
		
		if (!playerNames.contains(name))
		{
			failureReason = PacketLoginFailed.Reason.USER_NOT_FOUND;
			System.out.println("???: " + name);
		}
		else
		{
			synchronized (connectionLock)
			{
				final int player = playerNames.indexOf(name);
				if (playerIDToConnection.containsKey(player))
				{
					failureReason = PacketLoginFailed.Reason.ALREADY_LOGGED_IN;
				}
				else
				{
					playerIDToConnection.put(player, connection);
					System.out.println("User logged in: " + name);
					onSuccessfulLogin(player);
					connection.addPacketHandler(handlers.get(player));
					connectionLock.notify();
				}
			}
		}
		
		if (failureReason != null)
		{
			connection.sendPacket(new PacketLoginFailed(failureReason));
			connection.closeRequest();
		}
	}
	
	private void disconnectFromPlayer(int player)
	{
		synchronized (connectionLock)
		{
			System.out.println("Player disconnected: " + playerNames.get(player));
			if (playerIDToConnection.containsKey(player))
			{
				Connection c = playerIDToConnection.remove(player);
				c.removePacketHandler(handlers.get(player));
				c.closeRequest();
			}
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
