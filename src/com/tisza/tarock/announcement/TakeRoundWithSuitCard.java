package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class TakeRoundWithSuitCard extends TakeRoundWithCard
{
	private boolean isKing;
	
	TakeRoundWithSuitCard(int roundIndex, SuitCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
		isKing = cardToTakeWith.getValue() == 5;
	}

	public int getPoints(int winnerBid)
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
