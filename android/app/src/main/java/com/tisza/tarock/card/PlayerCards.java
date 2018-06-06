package com.tisza.tarock.card;

import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.card.sort.*;

import java.util.*;

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
		List<Card> result = new ArrayList<>();
		for (Card c : cards)
		{
			if (f.match(c))
			{
				result.add(c);
			}
		}
		return result;
	}

	public int getTarockCount()
	{
		return filter(new TarockFilter()).size();
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

		List<List<Card>> lists = new ArrayList<>();

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

		return Collections.EMPTY_LIST;
	}

	public boolean canBeThrown(boolean afterChange)
	{
		
		if (!afterChange)
		{
			List<Card> tarocks = filter(new TarockFilter());
			
			if (tarocks.size() < 2)
				return true;
			
			tarocks.remove(Card.getTarockCard(1));
			tarocks.remove(Card.getTarockCard(2));
			tarocks.remove(Card.getTarockCard(21));
			
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
			if (!cards.contains(Card.getSuitCard(suit, 5)))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public PlayerCards clone()
	{
		PlayerCards clone = new PlayerCards();
		clone.cards.addAll(cards);
		return clone;
	}
}
