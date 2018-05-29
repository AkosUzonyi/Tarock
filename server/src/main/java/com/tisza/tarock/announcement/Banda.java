package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Banda extends TakeCards
{
	private final int suit;
	
	Banda(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
	}

	@Override
	public String getName()
	{
		return "banda";
	}

	@Override
	public int getSuit()
	{
		return suit;
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	protected boolean hasToBeTaken(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
	}
	
	@Override
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

	@Override
	public int getPoints()
	{
		return 4;
	}

	@Override
	public boolean canBeSilent()
	{
		return false;
	}
}
