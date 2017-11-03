package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class EventAvailableBids extends Event
{
	private List<Integer> bids;
	
	public EventAvailableBids() {}
	
	public EventAvailableBids(List<Integer> b)
	{
		bids = b;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		bids = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
		{
			bids.add((int)dis.readByte());
		}
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(bids.size());
		for (int bid : bids)
		{
			dos.writeByte(bid);
		}
	}

	public void handle(EventHandler handler)
	{
		handler.availableBids(bids);
	}
}
