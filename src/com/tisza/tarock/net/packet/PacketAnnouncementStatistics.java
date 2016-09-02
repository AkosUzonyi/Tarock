package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.announcement.AnnouncementBase.Result;

public class PacketAnnouncementStatistics extends Packet
{
	private List<Entry> entries;
	
	PacketAnnouncementStatistics() {}
	
	public PacketAnnouncementStatistics(List<Entry> entries)
	{
		this.entries = entries;
	}

	public List<Entry> getEntries()
	{
		return entries;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		short size = dis.readShort();
		entries = new ArrayList<Entry>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement announcement = Announcements.getFromID(dis.readShort());
			int contraLevel = dis.readByte();
			AnnouncementBase.Result result = AnnouncementBase.Result.values()[dis.readByte()];
			int points = dis.readShort();
			entries.add(new Entry(announcement, contraLevel, result, points));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(entries.size());
		for (Entry e : entries)
		{
			dos.writeShort(e.getAnnouncement().getID());
			dos.writeByte(e.getContraLevel());
			dos.writeByte(e.getResult().ordinal());
			dos.writeShort(e.getPoints());
		}
	}
	
	public static class Entry
	{
		private Announcement announcement;
		private int contraLevel;
		private AnnouncementBase.Result result;
		private int points;
		
		public Entry(Announcement announcement, int contraLevel, Result result, int points)
		{
			super();
			this.announcement = announcement;
			this.contraLevel = contraLevel;
			this.result = result;
			this.points = points;
		}
		
		public Announcement getAnnouncement()
		{
			return announcement;
		}
		
		public int getContraLevel()
		{
			return contraLevel;
		}
		
		public AnnouncementBase.Result getResult()
		{
			return result;
		}
		
		public int getPoints()
		{
			return points;
		}
	}
}
