package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Banda extends TakeCards
{
	private int suit;
	
	Banda(int suit)
	{
		this.suit = suit;
	}

	public int getPoints(int winnerBid)
	{
		return 4;
	}

	public boolean canBeSilent()
	{
		return false;
	}

	protected List<Card> getCardsToTake()
	{
		List<Card> result = new ArrayList<Card>();
		for (int v = 1; v <= 5; v++)
		{
			result.add(new SuitCard(suit, v));
		}
		return result;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (!super.canBeAnnounced(announcing, team))
			return false;
		
		for (Banda banda : Announcements.bandak)
		{
			if (announcing.isAnnounced(team, banda))
			{
				return false;
			}
		}
		return true;
	}
}
