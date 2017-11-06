package com.tisza.tarock.message.event;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class EventPlayerCards extends Event
{
	private PlayerCards pc;
	
	public EventPlayerCards() {}
	
	public EventPlayerCards(PlayerCards pc)
	{
		this.pc = pc;
	}

	public void readData(DataInputStream dis) throws IOException
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

	public void writeData(DataOutputStream dos) throws IOException
	{
		List<Card> cards = pc.getCards();
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}

	public void handle(EventHandler handler)
	{
		handler.cardsChanged(pc);
	}
}
