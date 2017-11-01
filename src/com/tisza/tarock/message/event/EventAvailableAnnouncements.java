package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tisza.tarock.announcement.Announcement;
import com.tisza.tarock.announcement.Announcements;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.message.EventHandler;

public class EventAvailableAnnouncements extends Event
{
	private List<AnnouncementContra> announcements;
	
	public EventAvailableAnnouncements() {}
	
	public EventAvailableAnnouncements(List<AnnouncementContra> announcements)
	{
		this.announcements = announcements;
	}

	public void readData(DataInputStream dis) throws IOException
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

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(announcements.size());
		for (AnnouncementContra announcement : announcements)
		{
			dos.writeShort(Announcements.getID(announcement.getAnnouncement()));
			dos.writeByte(announcement.getContraLevel());
		}
	}

	public void handle(EventHandler handler)
	{
		handler.availableAnnouncements(announcements);
	}
}
