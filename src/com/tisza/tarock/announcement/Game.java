package com.tisza.tarock.announcement;

public class Game extends GamePoints
{
	protected int getMinPointsRequired()
	{
		return 48;
	}

	protected int getDefaultPoints()
	{
		return 1;
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	public int getID()
	{
		return 0;
	}
}
