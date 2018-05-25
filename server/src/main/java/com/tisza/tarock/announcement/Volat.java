package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;

public class Volat extends TakeCards
{
	Volat(){}

	public String getName()
	{
		return "volat";
	}

	protected boolean hasToBeTaken(Card card)
	{
		return true;
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
