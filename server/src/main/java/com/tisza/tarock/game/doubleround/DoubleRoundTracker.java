package com.tisza.tarock.game.doubleround;

public interface DoubleRoundTracker
{
	public void gameFinished();
	public void gameInterrupted();
	public int getCurrentMultiplier();

	public static DoubleRoundTracker createFromType(DoubleRoundType type)
	{
		switch (type)
		{
			case NONE: return new None();
			case PECULATING: return new Peculating();
			case STACKING: return new Stacking();
			case MULTIPLYING: return new Multiplying();
		}

		return null;
	}
}
