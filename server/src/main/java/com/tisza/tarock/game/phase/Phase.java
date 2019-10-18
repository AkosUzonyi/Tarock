package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

abstract class Phase implements ActionHandler
{
	protected final GameState game;

	public Phase(GameState game)
	{
		this.game = game;
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();

	@Override
	public boolean announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		return wrongPhase("announce");
	}

	@Override
	public boolean announcePassz(PlayerSeat player)
	{
		return wrongPhase("announcePassz");
	}

	@Override
	public boolean bid(PlayerSeat player, int bid)
	{
		return wrongPhase("bid");
	}

	@Override
	public boolean call(PlayerSeat player, Card card)
	{
		return wrongPhase("call");
	}

	@Override
	public boolean change(PlayerSeat player, List<Card> cards)
	{
		return wrongPhase("change");
	}

	@Override
	public boolean playCard(PlayerSeat player, Card card)
	{
		return wrongPhase("playCard");
	}

	@Override
	public boolean readyForNewGame(PlayerSeat player)
	{
		return wrongPhase("readyForNewGame");
	}

	@Override
	public boolean throwCards(PlayerSeat player)
	{
		return wrongPhase("throwCards");
	}

	@Override
	public boolean chat(PlayerSeat player, String message)
	{
		game.broadcastEvent(Event.chat(player, message));
		return true;
	}

	private boolean wrongPhase(String action)
	{
		System.err.println("phase: " + asEnum() + " does not support action: " + action);
		return false;
	}
}
