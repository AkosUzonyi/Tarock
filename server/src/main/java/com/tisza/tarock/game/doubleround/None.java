package com.tisza.tarock.game.doubleround;

class None implements DoubleRoundTracker
{
	@Override
	public DoubleRoundType getType()
	{
		return DoubleRoundType.NONE;
	}

	@Override
	public void gameFinished() {}

	@Override
	public void gameInterrupted() {}

	@Override
	public int getCurrentMultiplier()
	{
		return 1;
	}

	@Override
	public int getData()
	{
		return 0;
	}

	@Override
	public void setData(int data) {}
}
