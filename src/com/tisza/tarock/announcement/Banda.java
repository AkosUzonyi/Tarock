package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Banda extends TakeCards
{
	private int suit;
	
	Banda(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
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
		for (Banda banda : Announcements.bandak)
		{
			if (announcing.isAnnounced(team, banda))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing, team);
	}

	public int getPoints()
	{
		return 4;
	}

	public boolean canBeSilent()
	{
		return false;
	}
}
