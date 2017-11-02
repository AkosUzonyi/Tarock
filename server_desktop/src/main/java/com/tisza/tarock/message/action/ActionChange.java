package com.tisza.tarock.message.action;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class ActionChange extends Action
{
	private List<Card> cards;
	
	public ActionChange() {}
	
	public ActionChange(List<Card> cards)
	{
		this.cards = cards;
	}

	public List<Card> getCards()
	{
		return Collections.unmodifiableList(cards);
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(cards.size());
		for (Card c : cards)
		{
			dos.writeByte(c.getID());
		}
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

	public void handle(int player, ActionHandler handler)
	{
		handler.change(player, cards);
	}
}
