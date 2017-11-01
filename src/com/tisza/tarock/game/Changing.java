package com.tisza.tarock.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.card.filter.SkartableCardFilter;
import com.tisza.tarock.message.event.EventActionFailed;
import com.tisza.tarock.message.event.EventActionFailed.Reason;
import com.tisza.tarock.message.event.EventCardsThrown;
import com.tisza.tarock.message.event.EventChange;
import com.tisza.tarock.message.event.EventChangeDone;
import com.tisza.tarock.message.event.EventPlayerCards;
import com.tisza.tarock.message.event.EventSkartTarock;

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
			gameState.sendEvent(i, new EventChange(cardsFromTalon.get(i)));
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
	
	public void change(int player, List<Card> cardsToSkart)
	{
		if (donePlayer[player])
			return;
		
		PlayerCards skartingPlayerCards = gameState.getPlayerCards(player);
		List<Card> cardsFromTalonForPlayer = cardsFromTalon.get(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
		{
			gameState.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return;
		}
		
		List<Card> checkedSkartCards = new ArrayList<Card>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				gameState.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
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
		
		gameState.sendEvent(player, new EventPlayerCards(skartingPlayerCards));
		gameState.broadcastEvent(new EventChangeDone(player));

		if (isFinished())
		{
			gameState.broadcastEvent(new EventSkartTarock(tarockCounts));
			gameState.changePhase(new Calling(gameState));
		}
	}
	
	public void throwCards(int player)
	{
		PlayerCards cards = gameState.getPlayerCards(player);
		if (cards.canBeThrown(true))
		{
			gameState.broadcastEvent(new EventCardsThrown(player, cards));
			gameState.changePhase(new PendingNewGame(gameState, true));
		}
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
