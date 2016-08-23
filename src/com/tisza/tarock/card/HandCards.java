package com.tisza.tarock.card;

import java.util.*;

public class HandCards
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
	
	public int tarockCount()
	{
		int result = 0;
		for (Card c : cards)
		{
			if (c instanceof TarockCard)
			{
				result++;
			}
		}
		return result;
	}
	
	public List<? extends Card> getPlaceableCards(Card firstCard)
	{
		if (firstCard == null)
		{
			return getCards();
		}
		
		List<List<? extends Card>> lists = new ArrayList<List<? extends Card>>();
		
		if (firstCard instanceof SuitCard)
		{
			int firstCardSuit = ((SuitCard)firstCard).getSuit();
			List<? extends Card> sameSuitCards = getCardsWithSuit(firstCardSuit);
			lists.add(sameSuitCards);
		}
		
		lists.add(getTarocks());
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
	
	public List<? extends Card> getCards()
	{
		return new ArrayList<Card>(cards);
	}
	
	private List<? extends Card> getCardsWithSuit(int suit)
	{
		List<Card> matchedCards = new ArrayList<Card>();
		for (Card c : cards)
		{
			if (c instanceof SuitCard && ((SuitCard)c).getSuit() == suit)
			{
				matchedCards.add(c);
			}
		}
		return matchedCards;
	}
	
	private List<? extends Card> getTarocks()
	{
		List<Card> matchedCards = new ArrayList<Card>();
		for (Card c : cards)
		{
			if (c instanceof TarockCard)
			{
				matchedCards.add(c);
			}
		}
		return matchedCards;
	}
	
	public HandCards clone()
	{
		HandCards clone = new HandCards();
		clone.cards.addAll(cards);
		return clone;
	}
}
