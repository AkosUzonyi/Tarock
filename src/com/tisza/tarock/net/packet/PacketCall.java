package com.tisza.tarock.net.packet;

import java.io.*;

import com.tisza.tarock.card.*;

public class PacketCall extends PacketGameAction
{
	private Card card;
	
	PacketCall() {}
	
	public PacketCall(Card c, int player)
	{
		super(player);
		card = c;
	}

	public Card getCalledCard()
	{
		return card;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
		card = Card.fromId(dis.readByte());
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		super.writeData(dos);
		dos.writeByte(card.getID());
	}
}
