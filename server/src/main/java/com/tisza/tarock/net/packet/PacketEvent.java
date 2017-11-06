package com.tisza.tarock.net.packet;

import com.tisza.tarock.message.*;
import com.tisza.tarock.message.event.*;

import java.io.*;

public class PacketEvent extends Packet
{
	private Event event;
	
	PacketEvent() {}
	
	public PacketEvent(Event event)
	{
		this.event = event;
	}

	public Event getEvent()
	{
		return event;
	}

	protected void readData(DataInputStream dis) throws IOException
	{
		event = MessageIO.readEvent(dis);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		MessageIO.writeEvent(event, dos);
	}
}
