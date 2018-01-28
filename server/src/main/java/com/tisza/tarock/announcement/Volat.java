package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;

import java.util.Collection;

public class Volat extends TakeCards
{
	Volat(){}

	public String getName()
	{
		return "volat";
	}

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
