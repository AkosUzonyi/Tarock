package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szinesites extends TakeCards
{
	Szinesites(){}

	public int getPoints(int winnerBid)
	{
		return 5;
	}

	public boolean canBeSilent()
	{
		return false;
	}

	protected Collection<Card> getCardsToTake()
	{
		Collection<Card> result = new ArrayList<Card>();
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				result.add(new SuitCard(s, v));
			}
		}
		return result;
	}
}
