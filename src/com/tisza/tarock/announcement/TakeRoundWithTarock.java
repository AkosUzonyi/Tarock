package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

public class TakeRoundWithTarock extends TakeRoundWithCard
{
	TakeRoundWithTarock(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}

	public int getPoints(int winnerBid)
	{
		return 10 * (9 - getRoundIndex());
	}

	public boolean canBeSilent()
	{
		return getRoundIndex() == 8;
	}
	
	public boolean isShownToUser()
	{
		return false;
	}
}
