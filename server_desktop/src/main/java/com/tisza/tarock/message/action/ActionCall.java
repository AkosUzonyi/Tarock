package com.tisza.tarock.message.action;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.io.*;

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
