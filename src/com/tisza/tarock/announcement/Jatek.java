package com.tisza.tarock.announcement;

public class Jatek extends GamePoints
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
}
