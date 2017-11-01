package com.tisza.tarock.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.tisza.tarock.message.action.Action;
import com.tisza.tarock.message.action.ActionAnnounce;
import com.tisza.tarock.message.action.ActionAnnouncePassz;
import com.tisza.tarock.message.action.ActionBid;
import com.tisza.tarock.message.action.ActionCall;
import com.tisza.tarock.message.action.ActionChange;
import com.tisza.tarock.message.action.ActionPlayCard;
import com.tisza.tarock.message.action.ActionReadyForNewGame;
import com.tisza.tarock.message.action.ActionThrowCards;
import com.tisza.tarock.message.event.Event;
import com.tisza.tarock.message.event.EventActionFailed;
import com.tisza.tarock.message.event.EventAnnounce;
import com.tisza.tarock.message.event.EventAnnouncePassz;
import com.tisza.tarock.message.event.EventAnnouncementStatistics;
import com.tisza.tarock.message.event.EventAvailableAnnouncements;
import com.tisza.tarock.message.event.EventAvailableBids;
import com.tisza.tarock.message.event.EventAvailableCalls;
import com.tisza.tarock.message.event.EventBid;
import com.tisza.tarock.message.event.EventCall;
import com.tisza.tarock.message.event.EventCardsTaken;
import com.tisza.tarock.message.event.EventCardsThrown;
import com.tisza.tarock.message.event.EventChange;
import com.tisza.tarock.message.event.EventChangeDone;
import com.tisza.tarock.message.event.EventPendingNewGame;
import com.tisza.tarock.message.event.EventPhase;
import com.tisza.tarock.message.event.EventPlayCard;
import com.tisza.tarock.message.event.EventPlayerCards;
import com.tisza.tarock.message.event.EventSkartTarock;
import com.tisza.tarock.message.event.EventStartGame;
import com.tisza.tarock.message.event.EventTurn;

public class MessageIO
{
	private MessageIO(){}
	
	private static ClassIDMapping<Action> actions = new ClassIDMapping<Action>();
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
	
	public static void writeAction(Action action, DataOutputStream dos) throws IOException
	{
		dos.writeByte(actions.getID(action));
		action.writeData(dos);
	}
	
	public static Action readAction(DataInputStream dis) throws IOException
	{
		byte id = dis.readByte();
		System.out.println("--------------------id: " + id);
		Action action = actions.createFromID(id);
		action.readData(dis);
		return action;
	}
	
	static
	{
		actions.register(0, ActionBid.class);
		actions.register(1, ActionChange.class);
		actions.register(2, ActionCall.class);
		actions.register(3, ActionAnnounce.class);
		actions.register(4, ActionAnnouncePassz.class);
		actions.register(5, ActionPlayCard.class);
		actions.register(6, ActionReadyForNewGame.class);
		actions.register(7, ActionThrowCards.class);

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
