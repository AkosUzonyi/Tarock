package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szinesites extends TakeCards
{
	Szinesites(){}

	@Override
	public String getName()
	{
		return "szinesites";
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
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.volat))
			return false;
		
		return super.canBeAnnounced(announcing);
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
