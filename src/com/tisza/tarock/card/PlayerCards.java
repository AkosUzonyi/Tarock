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
	
	public List<Card> getPlaceableCards(Card firstCard)
	{
		if (firstCard == null)
		{
			return getCards();
		}
		
		List<List<? extends Card>> lists = new ArrayList<List<? extends Card>>();
		
		if (firstCard instanceof SuitCard)
		{
			int firstCardSuit = ((SuitCard)firstCard).getSuit();
			List<? extends Card> sameSuitCards = filter(new SuitFilter(firstCardSuit));
			lists.add(sameSuitCards);
		}
		
		lists.add(filter(new TarockFilter()));
		lists.add(getCards());
		
		for (List<? extends Card> list : lists)
		{
			if (!list.isEmpty())
			{
				return list;
			}
		}
		
		return new ArrayList<Card>();
	}
	
	public List<Card> getCards()
	{
		return cards;
	}
	
	public PlayerCards clone()
	{
		PlayerCards clone = new PlayerCards();
		clone.cards.addAll(cards);
		return clone;
	}
}
