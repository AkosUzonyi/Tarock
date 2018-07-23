package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class Szinesites extends TakeCards
{
	Szinesites(){}

	@Override
	public AnnouncementID getID()
	{
		return new AnnouncementID("szinesites");
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard;
	}
	
	@Override
	public int getPoints()
	{
		return 5;
	}
	
	@Override
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	@Override
	public boolean canBeSilent()
	{
		return false;
	}
}
