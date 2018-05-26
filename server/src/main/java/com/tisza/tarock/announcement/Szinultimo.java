package com.tisza.tarock.announcement;

import com.tisza.tarock.card.SuitCard;

public class Szinultimo extends Ultimo
{
	private boolean isKing;
	
	Szinultimo(int roundIndex, SuitCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
		isKing = cardToTakeWith.getValue() == 5;
	}

	@Override
	public int getPoints()
	{
		if (getRoundIndex() == 8)
		{
			return isKing ? 15 : 20;
		}
		else
		{
			return 10 * (9 - getRoundIndex()) + (isKing ? 0 : 10);
		}
	}
}
