package com.tisza.tarock.game.doubleround;

class Multiplying implements DoubleRoundTracker
{
	private int data;

	@Override
	public DoubleRoundType getType()
	{
		return DoubleRoundType.MULTIPLYING;
	}

	@Override
	public void gameFinished()
	{
		data <<= 8;
	}

	@Override
	public void gameInterrupted()
	{
		data++;
	}

	@Override
	public int getCurrentMultiplier()
	{
		int allInterruptions = 0;

		long d = data;
		for (int i = 0; i < 4; i++)
		{
			allInterruptions += d & 0xFF;
			d >>>= 8;
		}

		return 1 << allInterruptions;
	}

	@Override
	public int getData()
	{
		return data;
	}

	@Override
	public void setData(int data)
	{
		this.data = data;
	}
}
