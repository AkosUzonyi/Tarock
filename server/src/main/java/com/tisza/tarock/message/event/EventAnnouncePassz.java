package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventAnnouncePassz extends Event
{
	private int player;
	
	public EventAnnouncePassz() {}
	
	public EventAnnouncePassz(int player)
	{
		this.player = player;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
	}

	public void handle(EventHandler handler)
	{
		handler.passz(player);
	}
}
