package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Trull extends TakeCards
{
	Trull(){}

	public int getPoints(int winnerBid)
	{
		return 2;
	}

	public boolean canBeSilent()
	{
		return true;
	}

	protected Collection<Card> getCardsToTake()
	{
		return Card.honors;
	}
}
