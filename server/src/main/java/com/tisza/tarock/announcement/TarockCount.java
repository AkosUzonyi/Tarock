package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

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
	public AnnouncementID getID()
	{
		String name;
		switch (count)
		{
			case 8: name = "nyolctarokk"; break;
			case 9: name = "kilenctarokk"; break;
			default: throw new RuntimeException();
		}
		return new AnnouncementID(name);
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
