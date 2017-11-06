package com.tisza.tarock.message.event;

import com.tisza.tarock.message.*;

import java.io.*;

public class EventBid extends Event
{
	private int player;
	private int bid;
	
	public EventBid() {}
	
	public EventBid(int player, int bid)
	{
		this.player = player;
		this.bid = bid;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		bid = dis.readByte();
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		dos.writeByte(bid);
	}

	public void handle(EventHandler handler)
	{
		handler.bid(player, bid);
	}
}
