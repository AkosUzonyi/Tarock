package com.tisza.tarock.game.doubleround;

class Peculating implements DoubleRoundTracker
{
	private int remainingDoubleGames = 0;

	@Override
	public DoubleRoundType getType()
	{
		return DoubleRoundType.PECULATING;
	}

	@Override
	public void gameFinished()
	{
		if (remainingDoubleGames > 0)
			remainingDoubleGames--;
	}

	@Override
	public void gameInterrupted()
	{
		remainingDoubleGames = 4;
	}

	@Override
	public int getCurrentMultiplier()
	{
		return remainingDoubleGames > 0 ? 2 : 1;
	}

	@Override
	public int getData()
	{
		return remainingDoubleGames;
	}

	@Override
	public void setData(int data)
	{
		remainingDoubleGames = data;
	}
}
