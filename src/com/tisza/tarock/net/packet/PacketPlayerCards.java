package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketPlayerCards extends Packet
{
	private PlayerCards pc;
	
	PacketPlayerCards() {}
	
	public PacketPlayerCards(PlayerCards pc)
	{
		this.pc = pc;
	}

	public PlayerCards getPlayerCards()
	{
		return pc;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		pc = new PlayerCards();
		int size = dis.readByte();
		for (int i = 0; i < size; i++)
		{
			int id = dis.readByte();
			if (Card.isValidId(id))
			{
				pc.addCard(Card.fromId(id));
			}
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		List<Card> cards = pc.getCards();
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}
}
