package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

public class PacketAvailableBids extends Packet
{
	private List<Integer> bids;
	
	PacketAvailableBids() {}
	
	public PacketAvailableBids(List<Integer> b)
	{
		bids = b;
	}

	public List<Integer> getAvailableBids()
	{
		return bids;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		bids = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
		{
			bids.add((int)dis.readByte());
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(bids.size());
		for (int bid : bids)
		{
			dos.writeByte(bid);
		}
	}
}
