package com.tisza.tarock.message.event;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.io.*;

public class EventAnnounce extends Event
{
	private int player;
	private AnnouncementContra announcementContra;
	
	public EventAnnounce() {}
	
	public EventAnnounce(int player, AnnouncementContra announcement)
	{
		this.player = player;
		this.announcementContra = announcement;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		Announcement a = Announcements.getFromID(dis.readShort());
		int contraLevel = dis.readByte();
		announcementContra = new AnnouncementContra(a, contraLevel);
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		dos.writeShort(Announcements.getID(announcementContra.getAnnouncement()));
		dos.writeByte(announcementContra.getContraLevel());
	}

	public void handle(EventHandler handler)
	{
		handler.announce(player, announcementContra);
	}
}
