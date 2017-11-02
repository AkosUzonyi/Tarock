package com.tisza.tarock.net.packet;

import com.tisza.tarock.message.*;
import com.tisza.tarock.message.action.*;

import java.io.*;

public class PacketAction extends Packet
{
	private Action action;
	
	PacketAction() {}
	
	public PacketAction(Action action)
	{
		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		action = MessageIO.readAction(dis);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		MessageIO.writeAction(action, dos);
	}
}
