package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.announcement.AnnouncementBase.Result;

public class PacketAnnouncementStatistics extends Packet
{
	private List<Entry> selfEntries;
	private List<Entry> opponentEntries;
	
	PacketAnnouncementStatistics() {}
	
	public PacketAnnouncementStatistics(List<Entry> selfEntries, List<Entry> opponentEntries)
	{
		this.selfEntries = selfEntries;
		this.opponentEntries = opponentEntries;
	}

	public List<Entry> getSelfEntries()
	{
		return selfEntries;
	}

	public List<Entry> getOpponentEntries()
	{
		return opponentEntries;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		selfEntries = readEntries(dis);
		opponentEntries = readEntries(dis);
	}
	
	private List<Entry> readEntries(DataInputStream dis) throws IOException
	{
		short size = dis.readShort();
		List<Entry> entries = new ArrayList<Entry>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement announcement = Announcements.getFromID(dis.readShort());
			boolean isAnnounced = dis.readBoolean();
			int contraLevel = dis.readByte();
			int points = dis.readShort();
			entries.add(new Entry(announcement, isAnnounced, contraLevel, points));
		}
		return entries;
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		writeEntries(dos, selfEntries);
		writeEntries(dos, opponentEntries);
	}
	
	private void writeEntries(DataOutputStream dos, List<Entry> entries) throws IOException
	{
		dos.writeShort(entries.size());
		for (Entry e : entries)
		{
			dos.writeShort(e.getAnnouncement().getID());
			dos.writeBoolean(e.isAnnounced());
			dos.writeByte(e.getContraLevel());
			dos.writeShort(e.getPoints());
		}
	}
	
	public static class Entry
	{
		private Announcement announcement;
		private boolean isAnnounced;
		private int contraLevel;
		private int points;
		
		public Entry(Announcement announcement, boolean isAnnounced, int contraLevel, int points)
		{
			super();
			this.announcement = announcement;
			this.isAnnounced = isAnnounced;
			this.contraLevel = contraLevel;
			this.points = points;
		}
		
		public Announcement getAnnouncement()
		{
			return announcement;
		}
		
		public boolean isAnnounced()
		{
			return isAnnounced;
		}
		
		public int getContraLevel()
		{
			return contraLevel;
		}
		
		public int getPoints()
		{
			return points;
		}
	}
}
