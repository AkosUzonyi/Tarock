package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Nagyszincsalad extends Szincsalad
{
	Nagyszincsalad(int suit)
	{
		super(suit);
	}

	@Override
	public String getName()
	{
		return "nagyszincsalad";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.MAGAS;
	}

	@Override
	protected int getRoundCount()
	{
		return 3;
	}

	@Override
	protected int getPoints()
	{
		return 100;
	}
}
