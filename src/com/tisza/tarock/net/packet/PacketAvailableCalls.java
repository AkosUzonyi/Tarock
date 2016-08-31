package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketAvailableCalls extends Packet
{
	private List<Card> cards;
	
	PacketAvailableCalls() {}
	
	public PacketAvailableCalls(List<Card> c)
	{
		cards = c;
	}

	public List<Card> getAvailableCalls()
	{
		return cards;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		cards = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++)
		{
			cards.add(Card.fromId(dis.readByte()));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}
}
