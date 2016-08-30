package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.announcement.*;

public class PacketAnnounce extends PacketGameAction
{
	private Announcement announcement;
	
	public PacketAnnounce() {}
	
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
		announcement = Announcements.getFromID(dis.readShort());
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(announcement.getID());
	}
}
