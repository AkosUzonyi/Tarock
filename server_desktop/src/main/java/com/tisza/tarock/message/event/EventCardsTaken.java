package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventCardsTaken extends Event
{
	private int winnerPlayer;
	
	public EventCardsTaken(){}

	public EventCardsTaken(int byPlayer)
	{
		this.winnerPlayer = byPlayer;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		winnerPlayer = dis.readByte();
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(winnerPlayer);
	}

	public void handle(EventHandler handler)
	{
		handler.cardsTaken(winnerPlayer);
	}
}
