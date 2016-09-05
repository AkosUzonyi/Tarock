package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;

public class AnnouncementWithContra
{
	private final Announcement a;
	private int contraLevel;

	public AnnouncementWithContra(Announcement a)
	{
		this.a = a;
		this.contraLevel = -1;
	}

	public Announcement getAnnouncement()
	{
		return a;
	}
	
	public boolean isAnnounced()
	{
		return contraLevel >= 0;
	}
	
	public void announce(int newContraLevel)
	{
		if (contraLevel + 1 != newContraLevel)
			throw new IllegalStateException();
		
		contraLevel++;
	}
	
	public int getContraLevel()
	{
		if (!isAnnounced())
			throw new IllegalStateException();
		
		return contraLevel;
	}
	
	public boolean isSelfTeamNextContra()
	{
		return getContraLevel() % 2 == 1;
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + contraLevel;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AnnouncementWithContra other = (AnnouncementWithContra)obj;
		if (a == null)
		{
			if (other.a != null) return false;
		}
		else if (!a.equals(other.a)) return false;
		if (contraLevel != other.contraLevel) return false;
		return true;
	}
}
