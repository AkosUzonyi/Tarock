package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Zaroparos extends LastRounds
{
	Zaroparos(){}

	@Override
	public String getName()
	{
		return "zaroparos";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.MAGAS;
	}

	@Override
	protected int getRoundCount()
	{
		return 2;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		if (!(card instanceof TarockCard))
			return false;

		TarockCard tarockCard = (TarockCard)card;
		return tarockCard.getValue() == 1 || tarockCard.getValue() == 2;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Announcement a0 = Announcements.ultimok.get(Card.getTarockCard(1)).get(6);
		Announcement a1 = Announcements.ultimok.get(Card.getTarockCard(2)).get(6);
		if (!a0.canBeAnnounced(announcing))
			return false;
		if  (!a1.canBeAnnounced(announcing))
			return false;
		
		return super.canBeAnnounced(announcing);
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int t = 1; t <= 2; t++)
		{
			for (int ri = 8; ri > 6; ri--)
			{
				Announcement ultimo = Announcements.ultimok.get(Card.getTarockCard(t)).get(ri);
				announcing.clearAnnouncement(team, ultimo);
			}
		}
	}

	@Override
	protected int getPoints()
	{
		return 40;
	}
}
