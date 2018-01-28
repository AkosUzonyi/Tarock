package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;

import java.util.Collection;

public class Gameplay extends Phase
{
	private Round currentRound;
	
	public Gameplay(GameState gs)
	{
		super(gs);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.GAMEPLAY;
	}

	public void onStart()
	{
		currentRound = new Round(gameState.getBeginnerPlayer());
		gameState.getEventQueue().broadcast().turn(currentRound.getCurrentPlayer());
	}

	public boolean playCard(int player, Card card)
	{
		if (player != currentRound.getCurrentPlayer())
			return false;
		
		if (!getPlaceableCards().contains(card))
		{
			//gameState.sendEvent(player, new EventActionFailed(Reason.INVALID_CARD));
			return false;
		}
		
		gameState.getPlayerCards(player).removeCard(card);
		currentRound.placeCard(card);
		
		if (currentRound.isFinished())
		{
			gameState.addRound(currentRound);
			int winner = currentRound.getWinner();
			gameState.addWonCards(winner, currentRound.getCards());
			currentRound = gameState.areAllRoundsPassed() ? null : new Round(winner);
			
			gameState.getEventQueue().broadcast().cardsTaken(winner);
		}
		
		if (currentRound != null)
		{
			gameState.getEventQueue().broadcast().turn(currentRound.getCurrentPlayer());
		}
		else
		{
			gameState.changePhase(new PendingNewGame(gameState, false));
			gameState.sendStatistics();
		}

		return true;
	}
	
	private Collection<Card> getPlaceableCards()
	{
		PlayerCards pc = gameState.getPlayerCards(currentRound.getCurrentPlayer());
		Card firstCard = currentRound.getFirstCard();
		return pc.getPlaceableCards(firstCard);
	}
}
