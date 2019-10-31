package com.tisza.tarock.game.doubleround;

public interface DoubleRoundTracker
{
	public DoubleRoundType getType();
	public void gameFinished();
	public void gameInterrupted();
	public int getCurrentMultiplier();
	public int getData();
	public void setData(int data);

	public static DoubleRoundTracker createFromType(DoubleRoundType type)
	{
		switch (type)
		{
			case NONE: return new None();
			case PECULATING: return new Peculating();
			case STACKING: return new Stacking();
			case MULTIPLYING: return new Multiplying();
		}

		throw new Error("invalid double round type: " + type);
	}
}
