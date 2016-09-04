package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Volat extends TakeCards
{
	Volat(){}

	public int getPoints(int winnerBid)
	{
		return 6;
	}

	public boolean canBeSilent()
	{
		return true;
	}

	protected Collection<Card> getCardsToTake()
	{
		return Card.all;
	}
}
