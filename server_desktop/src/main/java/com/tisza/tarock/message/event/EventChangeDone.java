package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventChangeDone extends Event
{
	private int player;
	
	public EventChangeDone() {}
	
	public EventChangeDone(int player)
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
		handler.changeDone(player);
	}
}
