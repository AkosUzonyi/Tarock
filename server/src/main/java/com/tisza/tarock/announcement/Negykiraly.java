package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

import java.util.ArrayList;
import java.util.List;

public class Negykiraly extends TakeCards
{
	Negykiraly(){}

	public String getName()
	{
		return "negykiraly";
	}

	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getValue() == 5;
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
