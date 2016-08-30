package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketThrowCards extends PacketGameAction
{
	public PacketThrowCards(int player)
	{
		super(player);
	}

	protected void readData(DataInputStream dis) throws IOException
	{
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
	}
}
