package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

class Gameplay extends Phase
{
	private Trick currentTrick;
	
	public Gameplay(Game game)
	{
		super(game);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.GAMEPLAY;
	}

	@Override
	public void onStart()
	{
		currentTrick = new Trick(PlayerSeat.SEAT0);
		game.broadcastEvent(Event.turn(currentTrick.getCurrentPlayer()));

		game.sendInGameStatistics();
	}

	@Override
	public boolean playCard(PlayerSeat player, Card card)
	{
		if (player != currentTrick.getCurrentPlayer())
			return false;
		
		if (!getPlayableCards().contains(card))
		{
			//game.sendEvent(player, new EventActionFailed(Reason.INVALID_CARD));
			return false;
		}

		game.getPlayerCards(player).removeCard(card);
		currentTrick.placeCard(card);

		game.broadcastEvent(Event.playCard(player, card));
		
		if (currentTrick.isFinished())
		{
			game.addTrick(currentTrick);
			PlayerSeat winner = currentTrick.getWinner();
			game.addWonCards(winner, currentTrick.getCards());
			currentTrick = game.areAllTricksPassed() ? null : new Trick(winner);
			
			game.broadcastEvent(Event.cardsTaken(winner));
		}
		
		if (currentTrick != null)
		{
			game.broadcastEvent(Event.turn(currentTrick.getCurrentPlayer()));
		}
		else
		{
			game.changePhase(new PendingNewGame(game, false));
		}

		return true;
	}
	
	private Collection<Card> getPlayableCards()
	{
		PlayerCards pc = game.getPlayerCards(currentTrick.getCurrentPlayer());
		Card firstCard = currentTrick.getFirstCard();
		return pc.getPlayableCards(firstCard);
	}
}
