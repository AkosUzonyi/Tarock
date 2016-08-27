package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public final class AnnouncementSilentPair implements Announcement
{
	private final AnnouncementBase normalPair;
	
	public AnnouncementSilentPair(AnnouncementBase a)
	{
		normalPair = a;
	}

	public int calculatePoints(GameHistory gh, boolean callerTeam, boolean isSilent)
	{
		return normalPair.isSuccessful(gh, false) ? normalPair.getPoints() / 2 : 0;
	}

	public boolean isSilent()
	{
		return true;
	}
	
	public String getName()
	{
		return "Csendes " + normalPair.getName();
	}

	public int getID()
	{
		return -normalPair.getID();
	}
}
