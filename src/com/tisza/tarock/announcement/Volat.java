package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;

public class Volat extends TakeCards
{
	Volat(){}

	protected Collection<Card> getCardsToTake()
	{
		return Card.all;
	}

	public int getPoints()
	{
		return 6;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	public boolean canBeSilent()
	{
		return true;
	}
}
