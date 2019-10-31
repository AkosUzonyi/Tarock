package com.tisza.tarock.game.doubleround;

public interface DoubleRoundTracker
{
	DoubleRoundType getType();
	void gameFinished();
	void gameInterrupted();
	int getCurrentMultiplier();
	int getData();
	void setData(int data);

	static DoubleRoundTracker createFromType(DoubleRoundType type)
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
