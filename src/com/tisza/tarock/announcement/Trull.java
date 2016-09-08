package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Trull extends TakeCards
{
	Trull(){}

	protected Collection<Card> getCardsToTake()
	{
		return Card.honors;
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
