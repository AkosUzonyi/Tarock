package com.tisza.tarock.game;

import com.tisza.tarock.game.*;

public class AnnouncementResult
{
	private AnnouncementContra announcement;
	private int points;
	private Team team;

	public AnnouncementResult(AnnouncementContra announcement, int points, Team team)
	{
		super();
		this.announcement = announcement;
		this.points = points;
		this.team = team;
	}

	public AnnouncementContra getAnnouncementContra()
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
