package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Negykiraly extends TakeCards
{
	Negykiraly(){}

	public int getPoints(int winnerBid)
	{
		return 2;
	}

	public boolean canBeSilent()
	{
		return true;
	}

	protected List<Card> getCardsToTake()
	{
		List<Card> result = new ArrayList<Card>();
		for (int s = 0; s < 4; s++)
		{
			result.add(new SuitCard(s, 5));
		}
		return result;
	}
}
