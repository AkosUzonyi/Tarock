package com.tisza.tarock.announcement;

import java.util.*;

public class Announcements
{
	private static final Map<Integer, Announcement> idToAnnouncement = new HashMap<Integer, Announcement>();
	
	public static Collection<Announcement> getAll()
	{
		return idToAnnouncement.values();
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
		}
	}
	
	static
	{
		register(new Trull());
	}
}
