package com.tisza.tarock.announcement;

import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.filter.TarockFilter;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class TarockCount implements Announcement
{
	private int count;
	
	TarockCount(int count)
	{
		this.count = count;
	}

	public String getName()
	{
		switch (count)
		{
			case 8:
				return "nyolctarokk";
			case 9:
				return "kilenctarokk";
		}
		return "unknown";
	}

	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, this))
			return false;
		
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		return cards.filter(new TarockFilter()).size() == count;
	}

	public void onAnnounced(IAnnouncing announcing)
	{
	}
	
	public boolean canContra()
	{
		return false;
	}

	public boolean isShownInList()
	{
		return true;
	}

	public boolean requireIdentification()
	{
		return true;
	}
}
