package com.tisza.tarock.game.doubleround;

class Multiplying implements DoubleRoundTracker
{
	private int[] interruptionCounts = new int[4];
	private int pos = 0;

	@Override
	public void gameFinished()
	{
		pos++;
		interruptionCounts[pos] = 0;
	}

	@Override
	public void gameInterrupted()
	{
		interruptionCounts[pos]++;
	}

	@Override
	public int getCurrentMultiplier()
	{
		int allInterruptions = 0;

		for (int interruptionCount : interruptionCounts)
		{
			allInterruptions += interruptionCount;
		}

		return 1 << allInterruptions;
	}
}
