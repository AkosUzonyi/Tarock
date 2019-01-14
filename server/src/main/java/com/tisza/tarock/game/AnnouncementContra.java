package com.tisza.tarock.game;

import com.tisza.tarock.game.announcement.*;

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

	public String getID()
	{
		return (isAnnounced() ? contraLevel : "s") + announcement.getID();
	}

	public static AnnouncementContra fromID(String id)
	{
		Announcement announcement = Announcements.getByID(id.substring(1));
		int contraLevel = id.charAt(0) == 's' ? -1 : Integer.parseInt(id.substring(0, 1));

		return new AnnouncementContra(announcement, contraLevel);
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

	@Override
	public int compareTo(AnnouncementContra o)
	{
		if (contraLevel != o.contraLevel)
			return o.contraLevel - contraLevel;
		
		int myPos = Announcements.getPosition(announcement);
		int oPos = Announcements.getPosition(o.announcement);
		if (myPos != oPos)
			return myPos - oPos;
		
		return 0;
	}
}
