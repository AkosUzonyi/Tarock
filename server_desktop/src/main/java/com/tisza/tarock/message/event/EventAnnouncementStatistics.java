package com.tisza.tarock.message.event;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class EventAnnouncementStatistics extends Event
{
	private int selfGamePoints, opponentGamePoints;
	private List<Entry> selfEntries;
	private List<Entry> opponentEntries;
	private int sumPoints;
	private int[] points;
	
	public EventAnnouncementStatistics() {}
	
	public EventAnnouncementStatistics(int selfGamePoints, int opponentGamePoints, List<Entry> selfEntries, List<Entry> opponentEntries, int sumPoints, int[] points)
	{
		this.selfGamePoints = selfGamePoints;
		this.opponentGamePoints = opponentGamePoints;
		this.selfEntries = selfEntries;
		this.opponentEntries = opponentEntries;
		this.sumPoints = sumPoints;
		this.points = points;
	}
	
	public void readData(DataInputStream dis) throws IOException
	{
		selfGamePoints = dis.readByte();
		opponentGamePoints = dis.readByte();
		selfEntries = readEntries(dis);
		opponentEntries = readEntries(dis);
		sumPoints = dis.readInt();
		points = new int[4];
		for (int i = 0; i < 4; i++)
		{
			points[i] = dis.readInt();
		}
	}
	
	private List<Entry> readEntries(DataInputStream dis) throws IOException
	{
		short size = dis.readShort();
		List<Entry> entries = new ArrayList<Entry>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement a = Announcements.getFromID(dis.readShort());
			int contraLevel = dis.readByte();
			int points = dis.readShort();
			entries.add(new Entry(new AnnouncementContra(a, contraLevel), points));
		}
		return entries;
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(selfGamePoints);
		dos.writeByte(opponentGamePoints);
		writeEntries(dos, selfEntries);
		writeEntries(dos, opponentEntries);
		dos.writeInt(sumPoints);
		for (int i = 0; i < 4; i++)
		{
			dos.writeInt(points[i]);
		}
	}
	
	private void writeEntries(DataOutputStream dos, List<Entry> entries) throws IOException
	{
		dos.writeShort(entries.size());
		for (Entry e : entries)
		{
			dos.writeShort(Announcements.getID(e.getAnnouncementContra().getAnnouncement()));
			dos.writeByte(e.getAnnouncementContra().getContraLevel());
			dos.writeShort(e.getPoints());
		}
	}

	public void handle(EventHandler handler)
	{
		handler.statistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
	}
	
	public static class Entry
	{
		private AnnouncementContra announcement;
		private int points;
		
		public Entry(AnnouncementContra announcement, int points)
		{
			super();
			this.announcement = announcement;
			this.points = points;
		}
		
		public AnnouncementContra getAnnouncementContra()
		{
			return announcement;
		}
		
		public int getPoints()
		{
			return points;
		}
	}
}
