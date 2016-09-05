package com.tisza.tarock.net.packet;

import java.io.*;
import java.util.*;

public class PacketServerStatus extends Packet
{
	private List<String> connectedPlayers;
	
	PacketServerStatus() {}
	
	public PacketServerStatus(List<String> connectedPlayers)
	{
		this.connectedPlayers = connectedPlayers;
	}

	public List<String> getConnectedPlayers()
	{
		return connectedPlayers;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		int size = dis.readByte();
		connectedPlayers = new ArrayList<String>(size);
		for (int i = 0; i < 4; i++)
		{
			connectedPlayers.add(dis.readUTF());
		}
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		for (String name : connectedPlayers)
		{
			dos.writeUTF(name);
		}
	}
}
