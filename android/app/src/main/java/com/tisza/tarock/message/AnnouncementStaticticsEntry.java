package com.tisza.tarock.message;

import com.tisza.tarock.*;

public class AnnouncementStaticticsEntry
{
	private Announcement announcement;
	private int points;

	public AnnouncementStaticticsEntry(Announcement announcement, int points)
	{
		super();
		this.announcement = announcement;
		this.points = points;
	}

	public Announcement getAnnouncement()
	{
		return announcement;
	}

	public int getPoints()
	{
		return points;
	}
}
