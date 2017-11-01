package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionHandler;

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
