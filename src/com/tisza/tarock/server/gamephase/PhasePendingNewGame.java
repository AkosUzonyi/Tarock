package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.Invitation;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhasePendingNewGame implements GamePhase
{
	private GameSession game;
	private boolean doubleRound;
	private boolean[] ready = new boolean[4];
	
	public PhasePendingNewGame(GameSession g, boolean doubleRound)
	{
		game = g;
		this.doubleRound = doubleRound;
	}

	public void start()
	{
		game.broadcastPacket(new PacketReadyForNewGame());
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketReadyForNewGame)
		{
			ready[player] = true;
			if (allReady())
			{
				game.startNewGame(doubleRound);
			}
		}
	}
	
	private boolean allReady()
	{
		for (boolean r : ready)
		{
			if (!r) return false;
		}
		return true;
	}
}
