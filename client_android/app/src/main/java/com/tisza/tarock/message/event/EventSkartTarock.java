package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventSkartTarock extends Event
{
	private int[] counts;
	
	public EventSkartTarock() {}
	
	public EventSkartTarock(int[] count)
	{
		if (count.length != 4)
			throw new IllegalArgumentException();
		
		this.counts = count;
	}
	
	public void readData(DataInputStream dis) throws IOException
	{
		counts = new int[4];
		for (int i = 0; i < 4; i++)
		{
			counts[i] = dis.readByte();
		}
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		for (int c : counts)
		{
			dos.writeByte(c);
		}
	}

	public void handle(EventHandler handler)
	{
		handler.skartTarokk(counts);
	}
}
