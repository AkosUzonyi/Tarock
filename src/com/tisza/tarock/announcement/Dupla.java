package com.tisza.tarock.announcement;

public class Dupla extends GamePoints
{
	protected int getMinPointsRequired()
	{
		return 71;
	}

	protected int getDefaultPoints()
	{
		return 4;
	}

	protected boolean canBeSilent()
	{
		return true;
	}
}
