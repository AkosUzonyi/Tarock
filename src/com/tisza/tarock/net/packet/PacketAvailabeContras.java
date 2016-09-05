package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;

public class PacketAvailabeContras extends Packet
{
	private boolean canAnnounce;
	private List<Contra> contras;
	
	PacketAvailabeContras() {}
	
	public PacketAvailabeContras(boolean canAnnounce, List<Contra> contras)
	{
		this.canAnnounce = canAnnounce;
		this.contras = contras;
	}

	public boolean getCanAnnounce()
	{
		return canAnnounce;
	}

	public List<Contra> getAvailableContras()
	{
		return contras;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		canAnnounce = dis.readBoolean();
		
		int size = dis.readByte();
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
		dos.writeBoolean(canAnnounce);
		
		dos.writeByte(contras.size());
		for (Contra contra : contras)
		{
			dos.writeShort(contra.getAnnouncement().getID());
			dos.writeByte(contra.getLevel());
		}
	}
}
