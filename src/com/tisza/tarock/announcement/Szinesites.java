package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szinesites extends TakeCards
{
	Szinesites(){}

	protected Collection<Card> getCardsToTake()
	{
		Collection<Card> result = new ArrayList<Card>();
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				result.add(new SuitCard(s, v));
			}
		}
		return result;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (announcing.isAnnounced(team, Announcements.volat))
			return false;
		
		return super.canBeAnnounced(announcing, team);
	}

	public int getPoints()
	{
		return 5;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return false;
	}

	public boolean canBeSilent()
	{
		return false;
	}
}
