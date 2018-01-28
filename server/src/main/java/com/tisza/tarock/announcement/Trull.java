package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;

import java.util.Collection;

public class Trull extends TakeCards
{
	Trull(){}

	public String getName()
	{
		return "trull";
	}

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
