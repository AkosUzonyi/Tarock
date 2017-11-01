package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.EventHandler;

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
