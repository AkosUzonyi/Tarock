package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

import java.util.ArrayList;
import java.util.List;

public class Banda extends TakeCards
{
	private int suit;
	
	Banda(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
	}

	public String getName()
	{
		return "banda";
	}

	public int getSuit()
	{
		return suit;
	}

	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
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
