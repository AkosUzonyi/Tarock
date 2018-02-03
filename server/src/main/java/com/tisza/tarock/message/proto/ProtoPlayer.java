package com.tisza.tarock.message.proto;

import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import java.util.concurrent.*;

public class ProtoPlayer implements Player
{
	private int id;
	private String name;
	private Connection connection;
	private EventQueue eventQueue;
	private BlockingQueue<Action> actionQueue;

	public ProtoPlayer(int id, String name, Connection connection)
	{
		this.id = id;
		this.name = name;
		this.connection = connection;
		eventQueue = new ProtoEventQueue(connection);
	}

	public String getName()
	{
		return name;
	}

	public EventQueue getEventQueue()
	{
		return eventQueue;
	}

	public void setActionQueue(BlockingQueue<Action> actionQueue)
	{
		this.actionQueue = actionQueue;
	}

	public void processAction(ActionProto.Action action)
	{
		actionQueue.add(new ProtoAction(id, action));
	}
}
