package com.tisza.tarock.message.action;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.io.*;

public class ActionAnnounce extends Action
{
	private AnnouncementContra announcementContra;
	
	public ActionAnnounce() {}
	
	public ActionAnnounce(AnnouncementContra announcement)
	{
		this.announcementContra = announcement;
	}

	public AnnouncementContra getAnnouncement()
	{
		return announcementContra;
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(Announcements.getID(announcementContra.getAnnouncement()));
		dos.writeByte(announcementContra.getContraLevel());
	}

	public void readData(DataInputStream dis) throws IOException
	{
		Announcement a = Announcements.getFromID(dis.readShort());
		int contraLevel = dis.readByte();
		announcementContra = new AnnouncementContra(a, contraLevel);
	}

	public void handle(int player, ActionHandler handler)
	{
		handler.announce(player, announcementContra);
	}
}
