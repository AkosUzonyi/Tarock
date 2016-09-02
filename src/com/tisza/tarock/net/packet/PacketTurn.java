package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketTurn extends Packet
{
	private int player;
	private Type type;
	
	PacketTurn() {}
	
	public PacketTurn(int p, Type t)
	{
		player = p;
		type = t;
	}

	public int getPlayer()
	{
		return player;
	}

	public Type getType()
	{
		return type;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		player = dis.readByte();
		type = Type.values()[dis.readByte()];
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(player);
		dos.writeByte(type.ordinal());
	}
	
	public static enum Type
	{
		BID, CALL, CHANGE, ANNOUNCE, PLAY_CARD;
	}
}
