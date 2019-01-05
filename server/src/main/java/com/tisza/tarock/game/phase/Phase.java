package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

abstract class Phase implements ActionHandler
{
	protected final GameState game;
	protected final GameHistory history;

	public Phase(GameState game)
	{
		this.game = game;
		history = game.getHistory();
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();
	public abstract void requestHistory(PlayerSeat player, EventSender eventSender);

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		wrongPhase("announce");
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		wrongPhase("announcePassz");
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		wrongPhase("bid");
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		wrongPhase("call");
	}

	@Override
	public void change(PlayerSeat player, List<Card> cards)
	{
		wrongPhase("change");
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		wrongPhase("playCard");
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		wrongPhase("readyForNewGame");
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		wrongPhase("throwCards");
	}

	@Override
	public void chat(PlayerSeat player, String message)
	{
		game.getBroadcastEventSender().chat(player, message);
	}

	private void wrongPhase(String action)
	{
		System.err.println("phase: " + asEnum() + " does not support action: " + action);
	}
}
