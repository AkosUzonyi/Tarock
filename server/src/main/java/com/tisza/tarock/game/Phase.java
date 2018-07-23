package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
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

	public void requestHistory(PlayerSeat player)
	{
		EventSender eventSender = game.getPlayerEventSender(player);
		eventSender.startGame(player, game.getPlayerNames(), game.getGameType(), game.getBeginnerPlayer());
		eventSender.playerCards(game.getPlayerCards(player));
		history.sendCurrentStatusToPlayer(player, asEnum(), game.getPlayerEventSender(player));
		eventSender.phaseChanged(asEnum());
	}

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

	private void wrongPhase(String action)
	{
		System.err.println("phase: " + asEnum() + " does not support action: " + action);
	}
}
