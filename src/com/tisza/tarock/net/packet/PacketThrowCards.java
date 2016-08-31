package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketThrowCards extends PacketGameAction
{
	PacketThrowCards() {}
	
	public PacketThrowCards(int player)
	{
		super(player);
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		super.readData(dis);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		super.writeData(dos);
	}
}
