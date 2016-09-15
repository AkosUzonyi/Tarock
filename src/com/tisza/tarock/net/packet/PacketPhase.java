package com.tisza.tarock.net.packet;

import java.io.*;

public class PacketPhase extends Packet
{
	private Phase phase;
	
	PacketPhase() {}
	
	public PacketPhase(Phase phase)
	{
		this.phase = phase;
	}

	public Phase getPhase()
	{
		return phase;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		phase = Phase.values()[dis.readByte()];
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		dos.writeByte(phase.ordinal());
	}
	
	public static enum Phase
	{
		BIDDING, CALLING, CHANGING, ANNOUNCING, GAMEPLAY, END;
	}
}
