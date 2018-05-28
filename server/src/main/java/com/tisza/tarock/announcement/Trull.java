package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Trull extends TakeCards
{
	Trull(){}

	@Override
	public String getName()
	{
		return "trull";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return card.isHonor();
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
