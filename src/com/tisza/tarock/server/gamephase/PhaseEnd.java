package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseEnd implements GamePhase
{
	private GameSession game;
	
	public PhaseEnd(GameSession g)
	{
		game = g;
	}
	
	public void start()
	{
		game.startNewGame(false);
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}

}
