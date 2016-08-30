package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.card.*;

public class PacketPlayCard extends PacketGameAction
{
	private Card card;
	
	PacketPlayCard() {}
	
	public PacketPlayCard(Card c, int player)
	{
		super(player);
		card = c;
	}

	public Card getCard()
	{
		return card;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		card = Card.fromId(dis.readByte());
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(card.getID());
	}
}
