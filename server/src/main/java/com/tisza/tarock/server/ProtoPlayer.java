package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

import java.util.concurrent.*;

public class ProtoPlayer implements Player
{
	private String name;
	private ProtoEventSender eventSender = new ProtoEventSender();
	private BlockingQueue<Action> actionQueue = null;
	private PlayerSeat seat;

	public ProtoPlayer(String name)
	{
		this.name = name;
	}

	public void useConnection(ProtoConnection connection)
	{
		eventSender.useConnection(connection);

		if (connection != null)
		{
			actionQueue.add(handler -> handler.requestHistory(seat));
		}
	}

	public void queueAction(ActionProto.Action action)
	{
		actionQueue.add(new ProtoAction(seat, action));
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public EventSender getEventSender()
	{
		return eventSender;
	}

	@Override
	public void onAddedToGame(BlockingQueue<Action> actionQueue, PlayerSeat seat)
	{
		this.actionQueue = actionQueue;
		this.seat = seat;
	}

	@Override
	public void onRemovedFromGame()
	{
		actionQueue = null;
		seat = null;
	}
}
