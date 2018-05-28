package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Nagyszincsalad extends Szincsalad
{
	Nagyszincsalad(int suit)
	{
		super(suit);
	}

	@Override
	public String getName()
	{
		return "nagyszincsalad";
	}

	@Override
	protected int getRoundCount()
	{
		return 3;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		announcing.clearAnnouncement(team, Announcements.kisszincsaladok[getSuit()]);
	}

	@Override
	protected int getPoints()
	{
		return 100;
	}
}
