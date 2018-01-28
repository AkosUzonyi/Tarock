package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.card.filter.SkartableCardFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Changing extends Phase
{
	private static final SkartableCardFilter cardFilter = new SkartableCardFilter();
	
	private List<List<Card>> cardsFromTalon = null;
	private boolean[] donePlayer = new boolean[4];
	private int[] tarockCounts = new int[4];
	
	public Changing(GameState gs)
	{
		super(gs);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CHANGING;
	}
	
	public void onStart()
	{
		dealCardsFromTalon();
		for (int i = 0; i < 4; i++)
		{
			gameState.getEventQueue().toPlayer(i).cardsFromTalon(cardsFromTalon.get(i));
		}
	}
	
	private void dealCardsFromTalon()
	{
		cardsFromTalon = new ArrayList<List<Card>>(4);
		for (int i = 0; i < 4; i++)
		{
			cardsFromTalon.add(new ArrayList<Card>());
		}
		
		List<Card> cardsRemaining = new LinkedList<Card>(gameState.getTalon());
		for (int i = 0; i < 4; i++)
		{
			int player = (gameState.getBidWinnerPlayer() + i) % 4;
			
			int cardCount;
			if (player == gameState.getBidWinnerPlayer())
			{
				cardCount = gameState.getWinnerBid();
			}
			else
			{
				cardCount = (int)Math.ceil((float)cardsRemaining.size() / (4 - i));
			}
			
			for (int j = 0; j < cardCount; j++)
			{
				cardsFromTalon.get(player).add(cardsRemaining.remove(0));
			}
		}
	}
	
	public boolean change(int player, List<Card> cardsToSkart)
	{
		if (donePlayer[player])
			return false;
		
		PlayerCards skartingPlayerCards = gameState.getPlayerCards(player);
		List<Card> cardsFromTalonForPlayer = cardsFromTalon.get(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
		{
			//gameState.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return false;
		}
		
		List<Card> checkedSkartCards = new ArrayList<Card>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				//gameState.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
				return false;
			}
			
			if (checkedSkartCards.contains(c))
				return false;
			
			checkedSkartCards.add(c);
			
			if (!skartingPlayerCards.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
				return false;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
		{
			if (c instanceof TarockCard)
			{
				tarockCount++;
				if (c.equals(new TarockCard(20)))
				{
					gameState.setPlayerSkarted20(player);
				}
			}
			
			gameState.addCardToSkart(player == gameState.getBidWinnerPlayer() ? Team.CALLER : Team.OPPONENT, c);
		}
		tarockCounts[player] = tarockCount;
		
		skartingPlayerCards.getCards().addAll(cardsFromTalonForPlayer);
		skartingPlayerCards.getCards().removeAll(cardsToSkart);
		
		donePlayer[player] = true;
		
		gameState.getEventQueue().toPlayer(player).playerCards(skartingPlayerCards);
		gameState.getEventQueue().broadcast().changeDone(player);

		if (isFinished())
		{
			gameState.getEventQueue().broadcast().skartTarock(tarockCounts);
			gameState.changePhase(new Calling(gameState));
		}

		return false; //do not broadcast skarted cards
	}
	
	public boolean throwCards(int player)
	{
		PlayerCards cards = gameState.getPlayerCards(player);
		if (cards.canBeThrown(true))
		{
			gameState.changePhase(new PendingNewGame(gameState, true));
			return true;
		}
		return false;
	}
	
	public boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}
}
