package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

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
			Announcement a = Announcements.getFromID(dis.readShort());
			int contraLevel = dis.readByte();
			int points = dis.readShort();
			entries.add(new Entry(new AnnouncementContra(a, contraLevel), points));
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
			dos.writeShort(e.getAnnouncementContra().getAnnouncement().getID());
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
