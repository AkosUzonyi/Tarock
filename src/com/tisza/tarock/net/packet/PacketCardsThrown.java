package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketCardsThrown extends Packet
{
	private int player;
	private PlayerCards thrownCards;
	
	PacketCardsThrown() {}
	
	public PacketCardsThrown(int p, PlayerCards pc)
	{
		player = p;
		thrownCards = pc;
	}

	public PlayerCards getThrownCards()
	{
		return thrownCards;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		thrownCards = new PlayerCards();
		int size = dis.readByte();
		for (int i = 0; i < size; i++)
		{
			int id = dis.readByte();
			thrownCards.addCard(Card.fromId(id));
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		List<Card> cards = thrownCards.getCards();
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}
}
