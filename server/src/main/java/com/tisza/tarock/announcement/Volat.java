package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Volat extends TakeCards
{
	Volat(){}

	@Override
	public String getName()
	{
		return "volat";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return true;
	}

	@Override
	public int getPoints()
	{
		return 6;
	}
	
	@Override
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	@Override
	public boolean canBeSilent()
	{
		return true;
	}
}
