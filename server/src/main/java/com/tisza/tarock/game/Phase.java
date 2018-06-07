package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.util.*;

abstract class Phase implements ActionHandler
{
	protected final GameSession gameSession;
	protected final GameState currentGame;
	protected final GameHistory history;

	public Phase(GameSession gameSession)
	{
		this.gameSession = gameSession;
		currentGame = gameSession.getCurrentGame();
		history = gameSession.getCurrentHistory();
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();

	public void requestHistory(PlayerSeat player)
	{
		EventSender eventSender = gameSession.getPlayerEventSender(player);
		eventSender.startGame(player, gameSession.getPlayerNames());
		history.sendCurrentStatusToPlayer(player, asEnum(), gameSession.getPlayerEventSender(player));
		eventSender.phaseChanged(asEnum());
		eventSender.playerCards(currentGame.getPlayerCards(player));
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		wrongPhase();
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		wrongPhase();
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		wrongPhase();
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		wrongPhase();
	}

	@Override
	public void change(PlayerSeat player, List<Card> cards)
	{
		wrongPhase();
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		wrongPhase();
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		wrongPhase();
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		wrongPhase();
	}

	private void wrongPhase()
	{
		System.err.println("wrong phase");
	}
}
