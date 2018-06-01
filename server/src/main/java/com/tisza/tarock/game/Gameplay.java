package com.tisza.tarock.game;

import com.tisza.tarock.card.*;

import java.util.*;

class Gameplay extends Phase
{
	private Round currentRound;
	
	public Gameplay(GameSession gameSession)
	{
		super(gameSession);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.GAMEPLAY;
	}

	@Override
	public void onStart()
	{
		currentRound = new Round(currentGame.getBeginnerPlayer());
		gameSession.getBroadcastEventSender().turn(currentRound.getCurrentPlayer());
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		for (PlayerSeat cardPlayer = currentRound.getBeginnerPlayer(); cardPlayer != currentRound.getCurrentPlayer(); cardPlayer = cardPlayer.nextPlayer())
		{
			gameSession.getPlayerEventQueue(player).playCard(cardPlayer, currentRound.getCardByPlayer(cardPlayer));
		}
		gameSession.getPlayerEventQueue(player).turn(currentRound.getCurrentPlayer());
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		if (player != currentRound.getCurrentPlayer())
			return;
		
		if (!getPlaceableCards().contains(card))
		{
			//gameSession.sendEvent(player, new EventActionFailed(Reason.INVALID_CARD));
			return;
		}

		currentGame.getPlayerCards(player).removeCard(card);
		currentRound.placeCard(card);

		gameSession.getBroadcastEventSender().playCard(player, card);
		
		if (currentRound.isFinished())
		{
			history.registerRound(currentRound);
			currentGame.addRound(currentRound);
			PlayerSeat winner = currentRound.getWinner();
			currentGame.addWonCards(winner, currentRound.getCards());
			currentRound = currentGame.areAllRoundsPassed() ? null : new Round(winner);
			
			gameSession.getBroadcastEventSender().cardsTaken(winner);
		}
		
		if (currentRound != null)
		{
			gameSession.getBroadcastEventSender().turn(currentRound.getCurrentPlayer());
		}
		else
		{
			gameSession.changePhase(new PendingNewGame(gameSession, false));
			gameSession.sendStatistics();
		}
	}
	
	private Collection<Card> getPlaceableCards()
	{
		PlayerCards pc = currentGame.getPlayerCards(currentRound.getCurrentPlayer());
		Card firstCard = currentRound.getFirstCard();
		return pc.getPlaceableCards(firstCard);
	}
}
