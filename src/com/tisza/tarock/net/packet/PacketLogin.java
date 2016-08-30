package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketLogin extends Packet
{
	private String name;
	
	public PacketLogin() {}
	
	public PacketLogin(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		name = dis.readUTF();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(name);
	}
}
