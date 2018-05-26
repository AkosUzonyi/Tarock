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

	@Override
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

	@Override
	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, this))
			return false;
		
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		return cards.filter(new TarockFilter()).size() == count;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
	}
	
	@Override
	public boolean canContra()
	{
		return false;
	}

	@Override
	public boolean isShownInList()
	{
		return true;
	}

	@Override
	public boolean requireIdentification()
	{
		return true;
	}
}
