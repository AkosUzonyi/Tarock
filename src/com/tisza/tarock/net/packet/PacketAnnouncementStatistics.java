package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

public class PacketAnnouncementStatistics extends Packet
{
	private int selfGamePoints, opponentGamePoints;
	private List<Entry> selfEntries;
	private List<Entry> opponentEntries;
	private int[] points;
	
	PacketAnnouncementStatistics() {}
	
	public PacketAnnouncementStatistics(int selfGamePoints, int opponentGamePoints, List<Entry> selfEntries, List<Entry> opponentEntries, int[] points)
	{
		this.selfGamePoints = selfGamePoints;
		this.opponentGamePoints = opponentGamePoints;
		this.selfEntries = selfEntries;
		this.opponentEntries = opponentEntries;
		this.points = points;
	}

	public int getSelfGamePoints()
	{
		return selfGamePoints;
	}

	public int getOpponentGamePoints()
	{
		return opponentGamePoints;
	}

	public List<Entry> getSelfEntries()
	{
		return selfEntries;
	}

	public List<Entry> getOpponentEntries()
	{
		return opponentEntries;
	}

	public int[] getPoints()
	{
		return points;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		selfGamePoints = dis.readByte();
		opponentGamePoints = dis.readByte();
		selfEntries = readEntries(dis);
		opponentEntries = readEntries(dis);
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

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(selfGamePoints);
		dos.writeByte(opponentGamePoints);
		writeEntries(dos, selfEntries);
		writeEntries(dos, opponentEntries);
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
