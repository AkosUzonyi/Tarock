package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionHandler;

import java.util.List;

abstract class Phase implements ActionHandler
{
	protected final GameSession gameSession;
	protected final GameState currentGame;

	public Phase(GameSession gameSession)
	{
		this.gameSession = gameSession;
		currentGame = gameSession.getCurrentGame();
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();

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
