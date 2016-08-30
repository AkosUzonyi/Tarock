package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Changing
{
	private static final List<Card> forbiddenCards = new ArrayList<Card>();
	
	private List<List<Card>> cardsFromTalon = new ArrayList<List<Card>>();
	private boolean[] donePlayer = new boolean[4];
	private AllPlayersCards cardsAfter;
	
	public Changing(AllPlayersCards cards, List<Card> talon, int winnerPlayer, int winnerBid)
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
	
	public boolean skart(int player, Collection<Card> cardsToSkart)
	{
		if (donePlayer[player])
			return false;
		
		List<Card> cardsFromTalonForPlayer = new ArrayList<Card>(cardsFromTalon.get(player));
		PlayerCards pc = cardsAfter.getPlayerCards(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
			return false;
		
		for (Card c : cardsToSkart)
		{
			if (forbiddenCards.contains(c))
				return false;
			if (!pc.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
				return false;
		}
		
		for (Card c : cardsToSkart)
		{
			boolean x = pc.removeCard(c) || cardsFromTalonForPlayer.remove(c);
		}
		
		pc.getCards().addAll(cardsFromTalonForPlayer);
		
		if (pc.getCards().size() != 9)
			throw new Error();
		
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
