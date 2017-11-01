package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionHandler;

public class ActionPlayCard extends Action
{
	private Card card;
	
	public ActionPlayCard() {}
	
	public ActionPlayCard(Card c)
	{
		card = c;
	}

	public Card getCard()
	{
		return card;
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(card.getID());
	}

	public void readData(DataInputStream dis) throws IOException
	{
		card = Card.fromId(dis.readByte());
	}

	public void handle(int player, ActionHandler handler)
	{
		handler.playCard(player, card);
	}
}
