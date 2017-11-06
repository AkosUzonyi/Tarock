package com.tisza.tarock.message.event;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class EventCardsThrown extends Event
{
	private int player;
	private PlayerCards thrownCards;
	
	public EventCardsThrown() {}
	
	public EventCardsThrown(int player, PlayerCards pc)
	{
		this.player = player;
		thrownCards = pc;
	}

	public void readData(DataInputStream dis) throws IOException
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

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		List<Card> cards = thrownCards.getCards();
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}

	public void handle(EventHandler handler)
	{
		handler.cardsThrown(player, thrownCards);
	}
}
