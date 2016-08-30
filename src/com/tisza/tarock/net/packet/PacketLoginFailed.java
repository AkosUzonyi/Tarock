package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketLoginFailed extends Packet
{
	private String error;
	
	PacketLoginFailed() {}
	
	public PacketLoginFailed(String e)
	{
		error = e;
	}
	
	protected void readData(DataInputStream dis) throws IOException
	{
		error = dis.readUTF();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(error);
	}
}
