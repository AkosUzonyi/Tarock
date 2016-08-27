package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;


public class AnnouncementInstance
{
	private final Announcement announcement;
	private boolean callerTeam;
	private PlayerPairs playerPairs;
	private int contraLevel = 0;
	
	public AnnouncementInstance(Announcement a, PlayerPairs pp, boolean ct)
	{
		announcement = a;
		playerPairs = pp;
		callerTeam = ct;
	}
	
	public int calculatePoints(Gameplay gp)
	{
		return announcement.calculatePoints(gp, playerPairs, callerTeam, false) * (int)(Math.pow(2, contraLevel));
	}
	
	public Announcement getAnnouncement()
	{
		return announcement;
	}

	public int getContraLevel()
	{
		return contraLevel;
	}

	public boolean isCallerTeam()
	{
		return callerTeam;
	}

	public void contra()
	{
		contraLevel++;
	}
}
