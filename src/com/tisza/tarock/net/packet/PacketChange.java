package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketChange extends PacketGameAction
{
	private List<Card> cards;
	
	PacketChange() {}
	
	public PacketChange(List<Card> cards, int player)
	{
		super(player);
		this.cards = cards;
	}

	public List<Card> getCards()
	{
		return Collections.unmodifiableList(cards);
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		int size = dis.readByte();
		cards = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++)
		{
			int id = dis.readByte();
			cards.add(Card.fromId(id));
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
