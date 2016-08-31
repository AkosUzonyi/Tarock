package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketChange extends PacketGameAction
{
	private Collection<Card> cards;
	
	PacketChange() {}
	
	public PacketChange(Collection<Card> c, int player)
	{
		super(player);
		cards = c;
	}

	public Collection<Card> getCards()
	{
		return Collections.unmodifiableCollection(cards);
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		int size = dis.readByte();
		cards = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++)
		{
			cards.add(Card.fromId(dis.readByte()));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		super.writeData(dos);
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}
}
