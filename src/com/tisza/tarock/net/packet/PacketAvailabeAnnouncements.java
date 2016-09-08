package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

public class PacketAvailabeAnnouncements extends Packet
{
	private List<AnnouncementContra> announcements;
	
	PacketAvailabeAnnouncements() {}
	
	public PacketAvailabeAnnouncements(List<AnnouncementContra> announcements)
	{
		this.announcements = announcements;
	}

	public List<AnnouncementContra> getAvailableAnnouncements()
	{
		return announcements;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readShort();
		announcements = new ArrayList<AnnouncementContra>(size);
		for (int i = 0; i < size; i++)
		{
			Announcement a = Announcements.getFromID(dis.readShort());
			int contraLevel = dis.readByte();
			announcements.add(new AnnouncementContra(a, contraLevel));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(announcements.size());
		for (AnnouncementContra announcement : announcements)
		{
			dos.writeShort(Announcements.getID(announcement.getAnnouncement()));
			dos.writeByte(announcement.getContraLevel());
		}
	}
}
