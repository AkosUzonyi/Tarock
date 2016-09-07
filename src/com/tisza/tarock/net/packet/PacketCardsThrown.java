package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

import com.tisza.tarock.card.*;

public class PacketCardsThrown extends PacketGameAction
{
	private PlayerCards thrownCards;
	
	PacketCardsThrown() {}
	
	public PacketCardsThrown(int player, PlayerCards pc)
	{
		super(player);
		thrownCards = pc;
	}

	public PlayerCards getThrownCards()
	{
		return thrownCards;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		
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
		super.writeData(dos);
		
		List<Card> cards = thrownCards.getCards();
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}
}
