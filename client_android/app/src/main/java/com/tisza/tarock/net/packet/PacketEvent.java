package com.tisza.tarock.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.MessageIO;
import com.tisza.tarock.message.event.Event;

import android.app.Notification.MessagingStyle.Message;

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
