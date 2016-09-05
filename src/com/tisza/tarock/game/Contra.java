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
		return level % 2 == 1;
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + level;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Contra other = (Contra)obj;
		if (a == null)
		{
			if (other.a != null) return false;
		}
		else if (!a.equals(other.a)) return false;
		if (level != other.level) return false;
		return true;
	}
}
