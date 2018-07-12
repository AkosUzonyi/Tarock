package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

class Gameplay extends Phase
{
	private Round currentRound;
	
	public Gameplay(GameState game)
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
		currentRound = new Round(game.getBeginnerPlayer());
		game.broadcastEvent(Event.turn(currentRound.getCurrentPlayer()));

		game.calculateInGameStatistics();
		game.sendInGameStatistics();
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		if (player != currentRound.getCurrentPlayer())
			return;
		
		if (!getPlaceableCards().contains(card))
		{
			//game.sendEvent(player, new EventActionFailed(Reason.INVALID_CARD));
			return;
		}

		game.getPlayerCards(player).removeCard(card);
		currentRound.placeCard(card);

		game.broadcastEvent(Event.playCard(player, card));
		
		if (currentRound.isFinished())
		{
			history.registerRound(currentRound);
			game.addRound(currentRound);
			PlayerSeat winner = currentRound.getWinner();
			game.addWonCards(winner, currentRound.getCards());
			currentRound = game.areAllRoundsPassed() ? null : new Round(winner);
			
			game.broadcastEvent(Event.cardsTaken(winner));
		}
		
		if (currentRound != null)
		{
			game.broadcastEvent(Event.turn(currentRound.getCurrentPlayer()));
		}
		else
		{
			game.changePhase(new PendingNewGame(game, false));
		}
	}
	
	private Collection<Card> getPlaceableCards()
	{
		PlayerCards pc = game.getPlayerCards(currentRound.getCurrentPlayer());
		Card firstCard = currentRound.getFirstCard();
		return pc.getPlaceableCards(firstCard);
	}
}
