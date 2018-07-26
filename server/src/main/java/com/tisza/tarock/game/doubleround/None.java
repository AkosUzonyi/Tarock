package com.tisza.tarock.game.doubleround;

class None implements DoubleRoundTracker
{
	@Override
	public void gameFinished()
	{

	}

	@Override
	public void gameInterrupted()
	{

	}

	@Override
	public int getCurrentMultiplier()
	{
		return 1;
	}
}
