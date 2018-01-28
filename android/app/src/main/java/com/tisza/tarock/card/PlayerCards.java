package com.tisza.tarock.card;

import com.tisza.tarock.card.filter.CardFilter;
import com.tisza.tarock.card.filter.SuitFilter;
import com.tisza.tarock.card.filter.TarockFilter;
import com.tisza.tarock.card.sort.IDSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCards
{
	private List<Card> cards = new ArrayList<>();

	public PlayerCards() {}

	public PlayerCards(List<Card> cards)
	{
		this.cards.addAll(cards);
	}

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
	
	public void sort()
	{
		Collections.sort(cards, new IDSort());
	}

	public List<Card> getPlaceableCards(Card firstCard)
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
		
		if (!afterChange)
		{
			List<Card> tarocks = filter(new TarockFilter());
			
			if (tarocks.size() < 2)
				return true;
			
			tarocks.remove(new TarockCard(1));
			tarocks.remove(new TarockCard(2));
			tarocks.remove(new TarockCard(21));
			
			if (tarocks.size() == 0)
				return true;
		}
		
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
