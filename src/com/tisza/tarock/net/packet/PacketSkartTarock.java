package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketSkartTarock extends Packet
{
	private int[] counts;
	
	PacketSkartTarock() {}
	
	public PacketSkartTarock(int[] count)
	{
		if (count.length != 4)
			throw new IllegalArgumentException();
		
		this.counts = count;
	}
	
	public int[] getCounts()
	{
		return counts;
	}
	
	protected void readData(DataInputStream dis) throws IOException
	{
		counts = new int[4];
		for (int i = 0; i < 4; i++)
		{
			counts[i] = dis.readByte();
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		for (int c : counts)
		{
			dos.writeByte(c);
		}
	}

}
