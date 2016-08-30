package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

public class PacketStartGame extends Packet
{
	private int playerID;
	private List<String> names;
	
	PacketStartGame() {}
	
	public PacketStartGame(List<String> n, int id)
	{
		playerID = id;
		names = n;
	}

	public int getPlayerID()
	{
		return playerID;
	}

	public List<String> getNames()
	{
		return names;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		playerID = dis.readByte();
		names.clear();
		for (int i = 0; i < 4; i++)
		{
			names.add(dis.readUTF());
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(playerID);
		for (int i = 0; i < 4; i++)
		{
			dos.writeUTF(names.get(i));
		}
	}
}
