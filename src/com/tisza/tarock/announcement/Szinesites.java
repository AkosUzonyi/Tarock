package com.tisza.tarock.announcement;

import java.util.ArrayList;
import java.util.Collection;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

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
