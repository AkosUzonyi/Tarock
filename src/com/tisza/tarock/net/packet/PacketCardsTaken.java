package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketCardsTaken extends Packet
{
	private int winnerPlayer;
	
	PacketCardsTaken(){}

	public PacketCardsTaken(int byPlayer)
	{
		this.winnerPlayer = byPlayer;
	}

	public int getWinnerPlayer()
	{
		return winnerPlayer;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		winnerPlayer = dis.readByte();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(winnerPlayer);
	}
}
