package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.Bidding.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseGameplay implements GamePhase
{
	private GameSession game;
	private Gameplay gameplay;
	
	public PhaseGameplay(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getGameHistory().changing.getCardsAfter();
		int bp = game.getGameHistory().beginnerPlayer;
		gameplay = new Gameplay(cards, bp);
	}

	public void start()
	{
	}

	@Override
	public void packetFromPlayer(int player, Packet packet)
	{
	}
}
