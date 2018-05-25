package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

import java.util.ArrayList;
import java.util.Collection;

public class Szinesites extends TakeCards
{
	Szinesites(){}

	public String getName()
	{
		return "szinesites";
	}

	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard;
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.volat))
			return false;
		
		return super.canBeAnnounced(announcing);
	}

	public int getPoints()
	{
		return 5;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	public boolean canBeSilent()
	{
		return false;
	}
}
