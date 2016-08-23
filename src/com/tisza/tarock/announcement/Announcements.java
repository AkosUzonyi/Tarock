package com.tisza.tarock.announcement;

import java.util.*;

public class Announcements
{
	private static final Map<Integer, Announcement> idToAnnouncement = new HashMap<Integer, Announcement>();
	private static final List<Announcement> silentList = new ArrayList<Announcement>();
	
	public static Collection<Announcement> getAll()
	{
		return idToAnnouncement.values();
	}
	
	public static Collection<Announcement> getSilent()
	{
		return silentList;
	}
	
	public static Announcement getFromID(int id)
	{
		return idToAnnouncement.get(id);
	}
	
	private static void register(Announcement a)
	{
		int id = a.getID();
		if (idToAnnouncement.containsKey(id))
		{
			System.err.println("Duplicate announcement id");
		}
		else
		{
			idToAnnouncement.put(a.getID(), a);
			if (a.isSilent())
			{
				silentList.add(a);
			}
		}
	}
	
	static
	{
		register(new Trull());
		
		List<Announcement> nonSilent = new ArrayList<Announcement>(getAll());
		for (Announcement a : nonSilent)
		{
			if (a instanceof AnnouncementBase)
			{
				AnnouncementBase ab = (AnnouncementBase)a;
				if (ab.hasSilentPair())
				{
					register(new AnnouncementSilentPair(ab));
				}
			}
		}
	}
}
