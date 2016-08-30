package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseBidding implements GamePhase
{
	private GameSession game;
	private Bidding b;
	
	public PhaseBidding(GameSession g)
	{
		game = g;
		b = new Bidding(game.getGameHistory().dealing.getCards(), game.getGameHistory().beginnerPlayer);
	}

	public void start()
	{
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketBid)
		{
			PacketBid packetBid = ((PacketBid)packet);
			if (packetBid.getPlayer() == player)
			{
				if (b.bid(player, packetBid.getBid()))
				{
					game.broadcastPacket(packetBid);
				}
			}
		}
	}

}
