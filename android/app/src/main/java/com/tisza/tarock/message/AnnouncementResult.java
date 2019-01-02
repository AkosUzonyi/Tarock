package com.tisza.tarock.message;

import com.tisza.tarock.game.*;

public class AnnouncementResult
{
	private Announcement announcement;
	private int points;
	private Team team;

	public AnnouncementResult(Announcement announcement, int points, Team team)
	{
		super();
		this.announcement = announcement;
		this.points = points;
		this.team = team;
	}

	public Announcement getAnnouncement()
	{
		return announcement;
	}

	public int getPoints()
	{
		return points;
	}

	public Team getTeam()
	{
		return team;
	}
}
