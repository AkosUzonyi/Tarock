package com.tisza.tarock.game;

import com.tisza.tarock.game.announcement.*;

public class AnnouncementContra implements Comparable<AnnouncementContra>
{
	private final Announcement announcement;
	private final int contraLevel;

	public AnnouncementContra(Announcement a, int contraLevel)
	{
		if (a == null)
			throw new IllegalArgumentException("announcement cannot be null");

		this.announcement = a;
		this.contraLevel = contraLevel;
	}

	public String getID()
	{
		String contraString;
		if (contraLevel > 0)
			contraString = "K" + contraLevel;
		else if (contraLevel == 0)
			contraString = "";
		else
			contraString = "Ks";

		return announcement.getID() + contraString;
	}

	public static AnnouncementContra fromID(String id)
	{
		String announcementID;
		int contraLevel;
		int kIndex = id.indexOf('K');
		if (kIndex < 0)
		{
			contraLevel = 0;
			announcementID = id;
		}
		else
		{
			contraLevel = id.charAt(kIndex + 1) == 's' ? -1 : Integer.parseInt(id.substring(kIndex + 1, kIndex + 2));
			announcementID = id.substring(0, kIndex);
		}

		Announcement announcement = Announcements.getByID(announcementID);
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
		result = prime * result + announcement.hashCode();
		result = prime * result + contraLevel;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AnnouncementContra other = (AnnouncementContra)obj;
		return announcement.equals(other.announcement) && contraLevel == other.contraLevel;
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
