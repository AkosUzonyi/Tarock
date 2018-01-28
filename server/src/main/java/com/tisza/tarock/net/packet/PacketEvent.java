package com.tisza.tarock.net.packet;

import com.tisza.tarock.proto.EventProto.Event;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		event = Event.parseDelimitedFrom(dis);
	}

	protected void writeData(DataOutputStream dos) throws IOException
	{
		event.writeDelimitedTo(dos);
	}
}
