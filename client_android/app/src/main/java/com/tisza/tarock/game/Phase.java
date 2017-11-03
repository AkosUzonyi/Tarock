package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.util.*;

abstract class Phase implements ActionHandler
{
	protected final GameState gameState;

	public Phase(GameState gameState)
	{
		this.gameState = gameState;
	}
	
	public abstract PhaseEnum asEnum();
	public abstract void onStart();
	
	public void announce(int player, AnnouncementContra announcementContra)
	{
		wrongPhase();
	}
	
	public void announcePassz(int player)
	{
		wrongPhase();
	}
	
	public void bid(int player, int bid)
	{
		wrongPhase();
	}
	
	public void call(int player, Card card)
	{
		wrongPhase();
	}
	
	public void change(int player, List<Card> cards)
	{
		wrongPhase();
	}
	
	public void playCard(int player, Card card)
	{
		wrongPhase();
	}
	
	public void readyForNewGame(int player)
	{
		wrongPhase();
	}
	
	public void throwCards(int player)
	{
		wrongPhase();
	}
	
	private void wrongPhase()
	{
		System.err.println("wrong phase");
	}
}
