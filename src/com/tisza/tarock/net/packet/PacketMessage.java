package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketMessage extends Packet
{
	private String msg;
	
	PacketMessage() {}
	
	public PacketMessage(String msg)
	{
		this.msg = msg;
	}

	public String getMessage()
	{
		return msg;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		msg = dis.readUTF();
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeUTF(msg);
	}
}
