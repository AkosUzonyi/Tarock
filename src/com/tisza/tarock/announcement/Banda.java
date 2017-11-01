package com.tisza.tarock.announcement;

import java.util.ArrayList;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

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
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (Banda banda : Announcements.bandak)
		{
			if (announcing.isAnnounced(team, banda))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing);
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
