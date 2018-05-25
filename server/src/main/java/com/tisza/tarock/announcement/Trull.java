package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;

public class Trull extends TakeCards
{
	Trull(){}

	public String getName()
	{
		return "trull";
	}

	protected boolean hasToBeTaken(Card card)
	{
		return card.isHonor();
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
