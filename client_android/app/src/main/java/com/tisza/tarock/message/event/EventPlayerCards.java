package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.message.EventHandler;

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
