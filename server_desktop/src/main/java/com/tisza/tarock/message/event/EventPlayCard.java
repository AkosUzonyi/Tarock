package com.tisza.tarock.message.event;

import com.tisza.tarock.card.*;
import com.tisza.tarock.message.*;

import java.io.*;

public class EventPlayCard extends Event
{
	private int player;
	private Card card;
	
	public EventPlayCard() {}
	
	public EventPlayCard(Card c, int player)
	{
		this.player = player;
		card = c;
	}

	public void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		card = Card.fromId(dis.readByte());
	}

	public void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		dos.writeByte(card.getID());
	}

	public void handle(EventHandler handler)
	{
		handler.cardPlayed(player, card);
	}
}
