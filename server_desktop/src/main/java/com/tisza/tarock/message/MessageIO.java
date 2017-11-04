package com.tisza.tarock.message;

import com.tisza.tarock.message.event.*;

import java.io.*;

public class MessageIO
{
	private MessageIO(){}
	
	private static ClassIDMapping<Event> events = new ClassIDMapping<Event>();
	
	public static void writeEvent(Event event, DataOutputStream dos) throws IOException
	{
		dos.writeByte(events.getID(event));
		event.writeData(dos);
	}
	
	public static Event readEvent(DataInputStream dis) throws IOException
	{
		Event event = events.createFromID(dis.readByte());
		event.readData(dis);
		return event;
	}

	static
	{
		events.register(0, EventActionFailed.class);
		events.register(1, EventAnnounce.class);
		events.register(2, EventAvailableAnnouncements.class);
		events.register(3, EventAnnouncementStatistics.class);
		events.register(4, EventAnnouncePassz.class);
		events.register(5, EventAvailableBids.class);
		events.register(6, EventAvailableCalls.class);
		events.register(7, EventBid.class);
		events.register(8, EventCall.class);
		events.register(9, EventCardsTaken.class);
		events.register(10, EventCardsThrown.class);
		events.register(11, EventChange.class);
		events.register(12, EventChangeDone.class);
		events.register(13, EventPendingNewGame.class);
		events.register(14, EventPhase.class);
		events.register(15, EventPlayCard.class);
		events.register(16, EventPlayerCards.class);
		events.register(17, EventSkartTarock.class);
		events.register(18, EventStartGame.class);
		events.register(19, EventTurn.class);
}
}
