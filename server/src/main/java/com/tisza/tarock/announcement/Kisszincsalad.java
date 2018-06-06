package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Kisszincsalad extends Szincsalad
{
	Kisszincsalad(int suit)
	{
		super(suit);
	}

	@Override
	public String getName()
	{
		return "kisszincsalad";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	protected int getRoundCount()
	{
		return 2;
	}

	@Override
	protected int getPoints()
	{
		return 60;
	}
}
