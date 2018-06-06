package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;

import java.util.*;

class Changing extends Phase
{
	private static final SkartableCardFilter cardFilter = new SkartableCardFilter();
	
	private PlayerSeat.Map<List<Card>> cardsFromTalon = new PlayerSeat.Map<>();
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

		for (PlayerSeat player : PlayerSeat.getAll())
		{
			gameSession.getPlayerEventQueue(player).cardsFromTalon(cardsFromTalon.get(player));
		}
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		for (PlayerSeat onePlayer : PlayerSeat.getAll())
		{
			if (donePlayer.get(onePlayer))
			{
				gameSession.getPlayerEventQueue(player).changeDone(player);
			}
		}

		if (!donePlayer.get(player))
			gameSession.getPlayerEventQueue(player).cardsFromTalon(cardsFromTalon.get(player));
	}

	private void dealCardsFromTalon()
	{
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			cardsFromTalon.put(player, new ArrayList<>());
		}
		
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
			
			for (int j = 0; j < cardCount; j++)
			{
				cardsFromTalon.get(player).add(remainingCards.remove(0));
			}

			history.setCardsFromTalon(player, cardsFromTalon.get(player));

			player = player.nextPlayer();
		}
	}
	
	@Override
	public void change(PlayerSeat player, List<Card> cardsToSkart)
	{
		if (donePlayer.get(player))
			return;
		
		PlayerCards skartingPlayerCards = currentGame.getPlayerCards(player);
		List<Card> cardsFromTalonForPlayer = cardsFromTalon.get(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
		{
			//gameSession.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return;
		}
		
		List<Card> checkedSkartCards = new ArrayList<>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				//gameSession.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
				return;
			}
			
			if (checkedSkartCards.contains(c))
				return;
			
			checkedSkartCards.add(c);
			
			if (!skartingPlayerCards.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
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
		
		skartingPlayerCards.getCards().addAll(cardsFromTalonForPlayer);
		skartingPlayerCards.getCards().removeAll(cardsToSkart);
		
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
