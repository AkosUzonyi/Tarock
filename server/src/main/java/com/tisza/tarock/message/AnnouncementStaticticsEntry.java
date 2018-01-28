package com.tisza.tarock.message;

import com.tisza.tarock.game.AnnouncementContra;

public class AnnouncementStaticticsEntry
{
	private AnnouncementContra announcement;
	private int points;

	public AnnouncementStaticticsEntry(AnnouncementContra announcement, int points)
	{
		super();
		this.announcement = announcement;
		this.points = points;
	}

	public AnnouncementContra getAnnouncementContra()
	{
		return announcement;
	}

	public int getPoints()
	{
		return points;
	}
}
