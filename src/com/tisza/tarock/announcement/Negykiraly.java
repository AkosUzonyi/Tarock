package com.tisza.tarock.announcement;

import java.util.ArrayList;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;

public class Negykiraly extends TakeCards
{
	Negykiraly(){}

	protected List<Card> getCardsToTake()
	{
		List<Card> result = new ArrayList<Card>();
		for (int s = 0; s < 4; s++)
		{
			result.add(new SuitCard(s, 5));
		}
		return result;
	}

	public int getPoints()
	{
		return 2;
	}

	public boolean canBeSilent()
	{
		return true;
	}
}
