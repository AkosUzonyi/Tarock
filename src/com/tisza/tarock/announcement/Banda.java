package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Banda extends TakeCards
{
	private int suit;
	
	Banda(int suit)
	{
		this.suit = suit;
	}

	public int getPoints(int winnerBid)
	{
		return 4;
	}

	public boolean canBeSilent()
	{
		return false;
	}

	protected List<Card> getCardsToTake()
	{
		List<Card> result = new ArrayList<Card>();
		for (int v = 1; v <= 5; v++)
		{
			result.add(new SuitCard(suit, v));
		}
		return result;
	}
}
