package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;

public class Contra
{
	private final Announcement a;
	private final int level;

	public Contra(Announcement a, int level)
	{
		this.a = a;
		this.level = level;
	}

	public Announcement getAnnouncement()
	{
		return a;
	}
	
	public int getLevel()
	{
		return level;
	}

	public boolean isSelf()
	{
		return level % 2 == 0;
	}
}
