package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

public class Negykiraly extends TakeCards
{
	Negykiraly(){}

	@Override
	public String getName()
	{
		return "negykiraly";
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getValue() == 5;
	}

	@Override
	public int getPoints()
	{
		return 2;
	}

	@Override
	public boolean canBeSilent()
	{
		return true;
	}
}
