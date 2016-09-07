package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Dupla extends GamePoints
{
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (!super.canBeAnnounced(announcing, team))
			return false;
		
		if (announcing.isAnnounced(team, Announcements.dupla))
			return false;
		
		if (announcing.isAnnounced(team, Announcements.hosszuDupla))
			return false;
		
		return true;
	}

	protected int getMinPointsRequired()
	{
		return 71;
	}

	protected int getDefaultPoints()
	{
		return 4;
	}

	protected boolean canBeSilent()
	{
		return true;
	}
}
