package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;


public class AnnouncementInstance
{
	private final Announcement announcement;
	private GameHistory gameHistory;
	private boolean callerTeam;
	private int contraLevel = 0;
	
	public AnnouncementInstance(Announcement a, GameHistory gh, boolean ct)
	{
		announcement = a;
		gameHistory = gh;
		callerTeam = ct;
	}
	
	public int calculatePoints()
	{
		return announcement.calculatePoints(gameHistory, callerTeam, false) * (int)(Math.pow(2, contraLevel));
	}
	
	public void contra()
	{
		contraLevel++;
	}
}
