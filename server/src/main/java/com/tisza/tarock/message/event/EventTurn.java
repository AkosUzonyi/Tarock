package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventTurn extends Event
{
	private int player;
	
	public EventTurn() {}
	
	public EventTurn(int p)
	{
		player = p;
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
		handler.turn(player);
	}
}
