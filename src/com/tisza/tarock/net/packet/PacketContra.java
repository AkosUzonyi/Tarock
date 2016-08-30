package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

public class PacketContra extends PacketGameAction
{
	private Contra contra;
	
	PacketContra() {}
	
	public PacketContra(Contra c, int player)
	{
		super(player);
		contra = c;
	}

	public Contra getContra()
	{
		return contra;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		Announcement a = Announcements.getFromID(dis.readShort());
		int level = dis.readByte();
		contra = new Contra(a, level);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeShort(contra.getAnnouncement().getID());
		dos.writeByte(contra.getLevel());
	}
}
