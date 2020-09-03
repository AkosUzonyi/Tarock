package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;

public class HivatalbolKontraParti extends AnnouncementWrapper
{
	HivatalbolKontraParti()
	{
		super(new AnnouncementContra(Announcements.jatek, 1));
	}

	@Override
	public String getID()
	{
		return "hkp";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.shouldHkpBeAnnounced();
	}
}
