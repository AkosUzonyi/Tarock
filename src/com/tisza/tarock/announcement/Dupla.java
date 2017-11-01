package com.tisza.tarock.announcement;

import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class Dupla extends GamePoints
{
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.volat))
			return false;
		
		return super.canBeAnnounced(announcing);
	}

	protected int getMinPointsRequired()
	{
		return 71;
	}

	protected int getPoints()
	{
		return 4;
	}

	protected boolean canBeSilent()
	{
		return true;
	}
}
