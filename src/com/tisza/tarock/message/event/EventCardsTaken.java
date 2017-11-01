package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.EventHandler;

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
