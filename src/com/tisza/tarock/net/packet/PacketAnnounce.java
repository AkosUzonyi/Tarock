package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.announcement.*;

public class PacketAnnounce extends PacketGameAction
{
	private Announcement announcement;
	
	PacketAnnounce() {}
	
	public PacketAnnounce(Announcement a, int player)
	{
		super(player);
		announcement = a;
	}

	public Announcement getAnnouncement()
	{
		return announcement;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		short id = dis.readShort();
		if (id < 0)
		{
			announcement = null;
		}
		else
		{
			announcement = Announcements.getFromID(dis.readShort());
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		super.writeData(dos);
		if (announcement == null)
		{
			dos.writeShort(-1);
		}
		else
		{
			dos.writeShort(announcement.getID());
		}
	}
}
