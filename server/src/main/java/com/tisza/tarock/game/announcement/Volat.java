package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class Volat extends TakeCards
{
	Volat(){}

	@Override
	public AnnouncementID getID()
	{
		return new AnnouncementID("volat");
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return true;
	}

	@Override
	public int getPoints()
	{
		return 6;
	}
	
	@Override
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	@Override
	public boolean canBeSilent()
	{
		return true;
	}
}
