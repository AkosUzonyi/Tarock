package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.ActionHandler;

public class ActionBid extends Action
{
	private int bid;
	
	public ActionBid() {}
	
	public ActionBid(int bid)
	{
		this.bid = bid;
	}

	public int getBid()
	{
		return bid;
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(bid);
	}

	public void readData(DataInputStream dis) throws IOException
	{
		bid = dis.readByte();
	}

	public void handle(int player, ActionHandler handler)
	{
		handler.bid(player, bid);
	}
}
