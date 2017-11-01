package com.tisza.tarock.message.event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.EventHandler;

public class EventCall extends Event
{
	private int player;
	private Card card;
	
	public EventCall() {}
	
	public EventCall(int player, Card c)
	{
		this.player = player;
		card = c;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		int id = dis.readByte();
		card = Card.fromId(id);
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		dos.writeByte(card.getID());
	}

	public void handle(EventHandler handler)
	{
		handler.call(player, card);
	}
}
