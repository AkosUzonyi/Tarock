package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

public class PacketAvailabeAnnouncements extends Packet
{
	private List<Announcement> announcements;
	private List<Contra> contras;
	
	PacketAvailabeAnnouncements() {}
	
	public PacketAvailabeAnnouncements(List<Announcement> announcements, List<Contra> contras)
	{
		this.announcements = announcements;
		this.contras = contras;
	}

	public List<Announcement> getAvailableAnnouncements()
	{
		return announcements;
	}

	public List<Contra> getAvailableContras()
	{
		return contras;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		announcements = new ArrayList<Announcement>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement a = Announcements.getFromID(dis.readShort());
			announcements.add(a);
		}
		
		size = dis.readByte();
		contras = new ArrayList<Contra>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement a = Announcements.getFromID(dis.readShort());
			int level = dis.readByte();
			contras.add(new Contra(a, level));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(announcements.size());
		for (Announcement announcement : announcements)
		{
			dos.writeShort(announcement.getID());
		}
		
		dos.writeByte(contras.size());
		for (Contra contra : contras)
		{
			dos.writeShort(contra.getAnnouncement().getID());
			dos.writeByte(contra.getLevel());
		}
	}
}
