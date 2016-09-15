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
	
	private File pointsDir;
	private int[] points = new int[4];
	
	private Thread gameThread;
	
	public GameSession(int beginnerPlayer, Collection<String> names, File pointsDir)
	{
		if (beginnerPlayer < 0 || beginnerPlayer >= 4 || names.size() != 4)
			throw new IllegalArgumentException();
		
		playerNames = new ArrayList<String>(names);
		Collections.shuffle(playerNames);
		
		this.pointsDir = pointsDir;
		
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
		checkThread();
		
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
	
	public int[] getPoints()
	{
		return points;
	}
	
	public void addPoints(int[] pointsToAdd)
	{
		for (int i = 0; i < 4; i++)
		{
			points[i] += pointsToAdd[i];
			if (pointsDir != null)
			{
				try
				{
					File pointsFile = new File(pointsDir, playerNames.get(i));
					pointsFile.createNewFile();
					PrintStream ps = new PrintStream(pointsFile);
					ps.println(points[i]);
					ps.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void checkThread()
	{
		if (Thread.currentThread() != gameThread)
			throw new RuntimeException();
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
	
	private void sendStatusInfo()
	{
		List<String> connectedPlayerNames = new ArrayList<String>();
		for (int player : playerIDToConnection.keySet())
		{
			connectedPlayerNames.add(playerNames.get(player));
		}
		broadcastPacket(new PacketServerStatus(connectedPlayerNames));
	}
	
	private void onSuccessfulLogin(int player)
	{
		//sendStatusInfo();
		
		if (pointsDir != null)
		{
			try
			{
				File pointsFile = new File(pointsDir, playerNames.get(player));
				if (pointsFile.exists())
				{
					Scanner sc = new Scanner(pointsFile);
					points[player] = sc.nextInt();
					sc.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (currentGamePhase != null)
		{
			sendPacketToPlayer(player, new PacketStartGame(playerNames, player));
			currentGamePhase.playerLoggedIn(player);
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
			if (playerIDToConnection.containsKey(player))
			{
				Connection c = playerIDToConnection.remove(player);
				c.removePacketHandler(handlers.get(player));
				c.closeRequest();
				System.out.println("Player disconnected: " + playerNames.get(player));
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
