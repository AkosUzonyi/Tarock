package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;

public class AnnouncementContra implements Comparable<AnnouncementContra>
{
	private final Announcement announcement;
	private int contraLevel;

	public AnnouncementContra(Announcement a, int contraLevel)
	{
		if (a == null)
			throw new IllegalArgumentException();
		
		this.announcement = a;
		this.contraLevel = contraLevel;
	}

	public Announcement getAnnouncement()
	{
		return announcement;
	}
	
	public boolean isAnnounced()
	{
		return contraLevel >= 0;
	}
	
	public int getContraLevel()
	{
		return contraLevel;
	}
	
	public Team getNextTeamToContra(Team originalAnnouncer)
	{
		if (!isAnnounced())
			throw new IllegalStateException();
		return getContraLevel() % 2 == 0 ? originalAnnouncer : originalAnnouncer.getOther();
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((announcement == null) ? 0 : announcement.hashCode());
		result = prime * result + contraLevel;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AnnouncementContra other = (AnnouncementContra)obj;
		if (announcement == null)
		{
			if (other.announcement != null) return false;
		}
		else if (!announcement.equals(other.announcement)) return false;
		if (contraLevel != other.contraLevel) return false;
		return true;
	}

	public int compareTo(AnnouncementContra o)
	{
		if (contraLevel != o.contraLevel)
			return o.contraLevel - contraLevel;
		
		int myAID = announcement.getID();
		int oAID = o.announcement.getID();
		if (myAID != oAID)
			return myAID - oAID;
		
		return 0;
	}
}
