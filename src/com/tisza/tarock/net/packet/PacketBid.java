package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketBid extends PacketGameAction
{
	private int bid;
	
	PacketBid() {}
	
	public PacketBid(int bid, int player)
	{
		super(player);
		this.bid = bid;
	}

	public int getBid()
	{
		return bid;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		bid = dis.readByte();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		super.writeData(dos);
		dos.writeByte(bid);
	}
}
