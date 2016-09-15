package com.tisza.tarock.net.packet;

import java.io.*;


public class PacketTurn extends Packet
{
	private int player;
	
	PacketTurn() {}
	
	public PacketTurn(int p)
	{
		player = p;
	}

	public int getPlayer()
	{
		return player;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
	}
}
