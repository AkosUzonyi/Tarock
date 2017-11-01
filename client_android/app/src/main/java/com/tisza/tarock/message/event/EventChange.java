package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.EventHandler;

public class EventChange extends Event
{
	private List<Card> cards;
	
	public EventChange() {}
	
	public EventChange(List<Card> cards)
	{
		this.cards = cards;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		cards = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++)
		{
			int id = dis.readByte();
			cards.add(Card.fromId(id));
		}
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
	}

	public void handle(EventHandler handler)
	{
		handler.cardsFromTalon(cards);
	}
}
