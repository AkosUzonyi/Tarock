package com.tisza.tarock.game;

import java.util.Collection;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.message.event.EventActionFailed;
import com.tisza.tarock.message.event.EventActionFailed.Reason;
import com.tisza.tarock.message.event.EventCardsTaken;
import com.tisza.tarock.message.event.EventPlayCard;
import com.tisza.tarock.message.event.EventTurn;

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
		gameState.broadcastEvent(new EventTurn(currentRound.getCurrentPlayer()));
	}

	public void playCard(int player, Card card)
	{
		if (player != currentRound.getCurrentPlayer())
			return;
		
		if (!getPlaceableCards().contains(card))
		{
			gameState.sendEvent(player, new EventActionFailed(Reason.INVALID_CARD));
			return;
		}
		
		gameState.getPlayerCards(player).removeCard(card);
		currentRound.placeCard(card);
		
		gameState.broadcastEvent(new EventPlayCard(card, player));
		
		if (currentRound.isFinished())
		{
			gameState.addRound(currentRound);
			int winner = currentRound.getWinner();
			gameState.addWonCards(winner, currentRound.getCards());
			currentRound = gameState.areAllRoundsPassed() ? null : new Round(winner);
			
			gameState.broadcastEvent(new EventCardsTaken(winner));
		}
		
		if (currentRound != null)
		{
			gameState.broadcastEvent(new EventTurn(currentRound.getCurrentPlayer()));
		}
		else
		{
			gameState.changePhase(new PendingNewGame(gameState, false));
			gameState.sendStatistics();
		}
	}
	
	private Collection<Card> getPlaceableCards()
	{
		PlayerCards pc = gameState.getPlayerCards(currentRound.getCurrentPlayer());
		Card firstCard = currentRound.getFirstCard();
		return pc.getPlaceableCards(firstCard);
	}
}
