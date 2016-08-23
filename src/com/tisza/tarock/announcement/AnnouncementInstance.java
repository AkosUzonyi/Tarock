package com.tisza.tarock.announcement;


public class AnnouncementInstance
{
	private final Announcement announcement;
	private int player;
	private int contraLevel = 0;
	
	public AnnouncementInstance(Announcement a, int p)
	{
		announcement = a;
		player = p;
	}
	
	public int getPlayer()
	{
		return player;
	}
	
	public void contra()
	{
		contraLevel++;
	}
}
