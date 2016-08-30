package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseDealing implements GamePhase
{
	private GameSession game;
	private Dealing d;
	
	public PhaseDealing(GameSession g)
	{
		game = g;
		d = new Dealing();
	}
	
	public void start()
	{
		for (int i = 0; i < 4; i++)
		{
			game.sendPacketToPlayer(i, new PacketPlayerCards(d.getCards().getPlayerCards(i)));
		}
		game.getGameHistory().dealing = d;
		game.changeGamePhase(new PhaseBidding(game));
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}
}
