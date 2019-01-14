package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class TarockCount implements Announcement
{
	private final int count;
	
	TarockCount(int count)
	{
		if (count < 8 || count >= 10)
			throw new IllegalArgumentException();

		this.count = count;
	}

	public int getPoints()
	{
		switch (count)
		{
			case 8: return 1;
			case 9: return 2;
		}
		throw new RuntimeException();
	}

	@Override
	public String getID()
	{
		switch (count)
		{
			case 8: return "nyolctarokk";
			case 9: return "kilenctarokk";
		}
		throw new RuntimeException();
	}

	@Override
	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		if (announcing.getTarockCountAnnounced(announcing.getCurrentPlayer()) != null)
			return false;
		
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		return cards.getTarockCount() == count;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		announcing.announceTarockCount(announcing.getCurrentPlayer(), this);
	}
	
	@Override
	public boolean canContra()
	{
		return false;
	}

	@Override
	public boolean requireIdentification()
	{
		return true;
	}

	@Override
	public boolean shouldBeStored()
	{
		return false;
	}
}
