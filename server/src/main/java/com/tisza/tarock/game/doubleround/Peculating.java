package com.tisza.tarock.game.doubleround;

class Peculating implements DoubleRoundTracker
{
	private int remainingDoubleGames = 0;

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
}
