package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Changeing
{
	private static final List<Card> forbiddenCards = new ArrayList<Card>();
	
	private List<List<Card>> cardsFromTalon = new ArrayList<List<Card>>();
	private boolean[] donePlayer = new boolean[4];
	private AllPlayersCards cardsAfter;
	
	public Changeing(AllPlayersCards cards, List<Card> talon, int winnerPlayer, int winnerBid)
	{
		cardsAfter = cards;
		
		List<Card> cardsRemaining = new LinkedList<Card>(talon);
		for (int i = 0; i < 4; i++)
		{
			int p = (i + winnerPlayer) % 4;
			int cardCount;
			if (p == 0)
			{
				cardCount = winnerBid;
			}
			else
			{
				cardCount = (float)cardsRemaining.size() / (4 - i) > 1 ? 2 : 1;
			}
			
			List<Card> _cardsFromTalon = new ArrayList<Card>();
			for (int j = 0; j < cardCount; j++)
			{
				_cardsFromTalon.add(cardsRemaining.remove(0));
			}
			cardsFromTalon.add(_cardsFromTalon);
		}
	}
	
	public Collection<Card> getCardsObtainedFromTalon(int player)
	{
		return Collections.unmodifiableCollection(cardsFromTalon.get(player));
	}
	
	public boolean skart(int player, Collection<Card> cards)
	{
		if (donePlayer[player])
			return false;
		
		List<Card> cardsFromTalonForCurrentPlayer = cardsFromTalon.get(player);
		PlayerCards pc = cardsAfter.getPlayerCards(player);
		
		if (cards.size() != cardsFromTalonForCurrentPlayer.size())
			return false;
		
		if (!pc.getCards().containsAll(cards))
			return false;
		
		for (Card c : cards)
		{
			if (forbiddenCards.contains(c)) return false;
		}
		
		pc.getCards().removeAll(cards);
		pc.getCards().addAll(cardsFromTalonForCurrentPlayer);
		
		return true;
	}
	
	public AllPlayersCards getCardsAfter()
	{
		return cardsAfter;
	}
	
	public boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}

	static
	{
		for (int s = 0; s < 4; s++)
		{
			forbiddenCards.add(new SuitCard(s, 5));
		}
		forbiddenCards.add(new TarockCard(1));
		forbiddenCards.add(new TarockCard(2));
		forbiddenCards.add(new TarockCard(21));
		forbiddenCards.add(new TarockCard(22));
	}
}
