package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;

import java.util.*;

class Changing extends Phase
{
	private static final SkartableCardFilter cardFilter = new SkartableCardFilter();
	
	private PlayerSeat.Map<Boolean> donePlayer = new PlayerSeat.Map<>(false);
	private PlayerSeat.Map<Integer> tarockCounts = new PlayerSeat.Map<>();
	
	public Changing(GameSession gameSession)
	{
		super(gameSession);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CHANGING;
	}
	
	@Override
	public void onStart()
	{
		dealCardsFromTalon();
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		for (PlayerSeat otherPlayer : PlayerSeat.getAll())
		{
			if (donePlayer.get(otherPlayer))
			{
				gameSession.getPlayerEventQueue(player).changeDone(otherPlayer);
			}
		}
	}

	private void dealCardsFromTalon()
	{
		List<Card> remainingCards = new LinkedList<>(currentGame.getTalon());
		PlayerSeat player = currentGame.getBidWinnerPlayer();

		for (int i = 0; i < 4; i++)
		{
			int cardCount;
			if (player == currentGame.getBidWinnerPlayer())
			{
				cardCount = currentGame.getWinnerBid();
			}
			else
			{
				cardCount = (int)Math.ceil((float)remainingCards.size() / (4 - i));
			}

			List<Card> cardsFromTalon = remainingCards.subList(0, cardCount);
			PlayerCards playerCards = currentGame.getPlayerCards(player);
			playerCards.addCards(cardsFromTalon);
			gameSession.getPlayerEventQueue(player).playerCards(playerCards);
			history.setCardsFromTalon(player, cardsFromTalon);
			cardsFromTalon.clear();

			player = player.nextPlayer();
		}
	}
	
	@Override
	public void change(PlayerSeat player, List<Card> cardsToSkart)
	{
		if (donePlayer.get(player))
			return;
		
		PlayerCards skartingPlayerCards = currentGame.getPlayerCards(player);

		if (skartingPlayerCards.size() - cardsToSkart.size() != GameSession.ROUND_COUNT)
		{
			//gameSession.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return;
		}
		
		Set<Card> checkedSkartCards = new HashSet<>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				//gameSession.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
				return;
			}
			
			if (!checkedSkartCards.add(c))
				return;

			if (!skartingPlayerCards.hasCard(c))
				return;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
		{
			if (c instanceof TarockCard)
			{
				tarockCount++;
				if (c.equals(Card.getTarockCard(20)))
				{
					currentGame.setPlayerSkarted20(player);
				}
			}

			currentGame.addCardToSkart(player == currentGame.getBidWinnerPlayer() ? Team.CALLER : Team.OPPONENT, c);
		}
		tarockCounts.put(player, tarockCount);
		
		skartingPlayerCards.removeCards(cardsToSkart);
		donePlayer.put(player, true);
		history.setCardsSkarted(player, cardsToSkart);
		gameSession.getPlayerEventQueue(player).playerCards(skartingPlayerCards);
		gameSession.getBroadcastEventSender().changeDone(player);

		if (isFinished())
		{
			gameSession.getBroadcastEventSender().skartTarock(tarockCounts);
			gameSession.changePhase(new Calling(gameSession));
		}
	}
	
	@Override
	public void throwCards(PlayerSeat player)
	{
		PlayerCards cards = currentGame.getPlayerCards(player);
		if (cards.canBeThrown(true))
		{
			gameSession.getBroadcastEventSender().throwCards(player);
			gameSession.changePhase(new PendingNewGame(gameSession, true));
		}
	}
	
	private boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}
}
