package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;


public class AnnouncementInstance
{
	private final Announcement announcement;
	private Team team;
	private PlayerPairs playerPairs;
	private int contraLevel = 0;
	
	public AnnouncementInstance(Announcement a, PlayerPairs pp, Team t)
	{
		announcement = a;
		playerPairs = pp;
		team = t;
	}
	
	public int calculatePoints(Gameplay gp)
	{
		return announcement.calculatePoints(gp, playerPairs, team, false) * (int)(Math.pow(2, contraLevel));
	}
	
	public Announcement getAnnouncement()
	{
		return announcement;
	}

	public Team getTeam()
	{
		return team;
	}

	public int getContraLevel()
	{
		return contraLevel;
	}

	public void contra()
	{
		contraLevel++;
	}
}
