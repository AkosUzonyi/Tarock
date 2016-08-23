package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public final class AnnouncementSilentPair implements Announcement
{
	private final AnnouncementBase normalPair;
	
	public AnnouncementSilentPair(AnnouncementBase a)
	{
		normalPair = a;
	}

	public int calculatePoints(Gameplay gp, int player0, int player1)
	{
		return normalPair.isSuccessful(gp, player0, player1) ? normalPair.getPoints() / 2 : 0;
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
