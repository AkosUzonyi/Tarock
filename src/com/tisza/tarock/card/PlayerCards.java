package com.tisza.tarock.card;

import java.util.*;

import com.tisza.tarock.card.filter.*;

public class PlayerCards
{
	private List<Card> cards = new ArrayList<Card>();

	public void addCard(Card c)
	{
		cards.add(c);
	}

	public boolean removeCard(Card c)
	{
		return cards.remove(c);
	}

	public boolean hasCard(Card c)
	{
		return cards.contains(c);
	}

	public List<Card> filter(CardFilter f)
	{
		List<Card> result = new ArrayList<Card>();
		for (Card c : cards)
		{
			if (f.match(c))
			{
				result.add(c);
			}
		}
		return result;
	}

	public List<Card> getCards()
	{
		return cards;
	}

	public Collection<Card> getPlaceableCards(Card firstCard)
	{
		if (firstCard == null)
		{
			return getCards();
		}

		List<List<Card>> lists = new ArrayList<List<Card>>();

		if (firstCard instanceof SuitCard)
		{
			int firstCardSuit = ((SuitCard)firstCard).getSuit();
			List<Card> sameSuitCards = filter(new SuitFilter(firstCardSuit));
			lists.add(sameSuitCards);
		}

		lists.add(filter(new TarockFilter()));
		lists.add(getCards());

		for (List<Card> list : lists)
		{
			if (!list.isEmpty())
			{
				return list;
			}
		}

		return new ArrayList<Card>();
	}

	public boolean canBeThrown(boolean afterChange)
	{
		if (!afterChange && filter(new TarockFilter()).size() < 2)
			return true;
		
		if (hasFourKings())
			return true;
		
		return false;
	}
	
	private boolean hasFourKings()
	{
		for (int suit = 0; suit < 4; suit++)
		{
			if (!cards.contains(new SuitCard(suit, 5)))
			{
				return false;
			}
		}
		return true;
	}

	public PlayerCards clone()
	{
		PlayerCards clone = new PlayerCards();
		clone.cards.addAll(cards);
		return clone;
	}
}
