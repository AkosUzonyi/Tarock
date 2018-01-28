package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionHandler;

import java.util.List;

abstract class Phase implements ActionHandler
{
	protected final GameState gameState;

	public Phase(GameState gameState)
	{
		this.gameState = gameState;
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();

	public boolean announce(int player, AnnouncementContra announcementContra)
	{
		return wrongPhase();
	}

	public boolean announcePassz(int player)
	{
		return wrongPhase();
	}

	public boolean bid(int player, int bid)
	{
		return wrongPhase();
	}

	public boolean call(int player, Card card)
	{
		return wrongPhase();
	}

	public boolean change(int player, List<Card> cards)
	{
		return wrongPhase();
	}

	public boolean playCard(int player, Card card)
	{
		return wrongPhase();
	}

	public boolean readyForNewGame(int player)
	{
		return wrongPhase();
	}

	public boolean throwCards(int player)
	{
		return wrongPhase();
	}

	private boolean wrongPhase()
	{
		System.err.println("wrong phase");
		return false;
	}
}
