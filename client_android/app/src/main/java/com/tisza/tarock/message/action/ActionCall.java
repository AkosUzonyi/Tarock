package com.tisza.tarock.message.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionHandler;

public class ActionCall extends Action
{
	private Card card;
	
	public ActionCall() {}
	
	public ActionCall(Card c)
	{
		card = c;
	}

	public Card getCalledCard()
	{
		return card;
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(card.getID());
	}

	public void readData(DataInputStream dis) throws IOException
	{
		int id = dis.readByte();
		card = Card.fromId(id);
	}

	public void handle(int player, ActionHandler handler)
	{
		handler.call(player, card);
	}
}
