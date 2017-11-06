package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketLoginFailed extends Packet
{
	private Reason reason;
	
	PacketLoginFailed() {}
	
	public PacketLoginFailed(Reason reason)
	{
		this.reason = reason;
	}
	
	protected void readData(DataInputStream dis) throws IOException
	{
		reason = Reason.values()[dis.readByte()];
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(reason.ordinal());
	}
	
	public static enum Reason
	{
		USER_NOT_FOUND, INVALID_PASSWORD, SERVER_FULL, ALREADY_LOGGED_IN, CONNECTION_ERROR;
	}
}
