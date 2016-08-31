package com.tisza.tarock.server;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import com.tisza.tarock.announcement.*;
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
				packetFromPlayer(pp.player, pp.packet);
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

	public void evaluatePoints()
	{
		checkThread();
		
		int pointsForCallerTeam = 0;
		
		Gameplay gp = currentGame.gameplay;
		PlayerPairs pp = currentGame.calling.getPlayerPairs();
		int winnerBid = currentGame.bidding.getWinnerBid();
		
		Map<Announcement, AnnouncementState> announcementStates = currentGame.announcing.getAnnouncementStates();
		for (Map.Entry<Announcement, AnnouncementState> announcementEntry : announcementStates.entrySet())
		{
			Announcement a = announcementEntry.getKey();
			AnnouncementState as = announcementEntry.getValue();
			for (Team t : Team.values())
			{
				AnnouncementState.PerTeam aspt = as.team(t);
				int points = a.calculatePoints(gp, pp, t, winnerBid, aspt.isAnnounced()) * (int)Math.pow(2, aspt.getContraLevel());
				pointsForCallerTeam += points * (t == Team.CALLER ? 1 : -1);
			}
		}
		
		points.addPoints(pointsForCallerTeam, pp.getCaller(), pp.getCalled());
		
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

	private void packetFromPlayer(int player, Packet packet)
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
		for (int i = 0; i < 4; i++)
		{
			disconnectFromPlayer(i);
		}
		gameThread.interrupt();
		currentGame = null;
	}
	
	public void loginAuthorized(String name, Connection connection)
	{
		String failure = null;
		
		if (!playerNames.contains(name))
		{
			failure = "You are not part of this game";
			System.out.println("???: " + name);
		}
		else
		{
			synchronized (connectionLock)
			{
				final int player = playerNames.indexOf(name);
				if (playerIDToConnection.containsKey(player))
				{
					failure = "User already logged in";
				}
				else
				{
					playerIDToConnection.put(player, connection);
					System.out.println("User logged in: " + name);
					connection.addPacketHandler(handlers.get(player));
					connectionLock.notify();
				}
			}
		}
		
		if (failure != null)
		{
			connection.sendPacket(new PacketLoginFailed(failure));
			connection.closeRequest();
		}
	}
	
	private void disconnectFromPlayer(int player)
	{
		synchronized (connectionLock)
		{
			System.out.println("Player disconnected: " + player);
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
