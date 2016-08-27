package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;


public class AnnouncementInstance
{
	private final Announcement announcement;
	private boolean callerTeam;
	private int contraLevel = 0;
	
	public AnnouncementInstance(Announcement a, boolean ct)
	{
		announcement = a;
		callerTeam = ct;
	}
	
	public int calculatePoints(GameHistory gameHistory)
	{
		return announcement.calculatePoints(gameHistory, callerTeam, false) * (int)(Math.pow(2, contraLevel));
	}
	
	public void contra()
	{
		contraLevel++;
	}
}
