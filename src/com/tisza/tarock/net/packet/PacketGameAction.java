package com.tisza.tarock.net.packet;

import java.io.*;

public abstract class PacketGameAction extends Packet
{
	private int player;
	
	PacketGameAction() {}
	
	public PacketGameAction(int player)
	{
		this.player = player;
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
