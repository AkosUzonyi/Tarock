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
	private ActionHandler actionHandler;
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
			actionHandler.requestHistory(seat);
		}
	}

	public void queueAction(ActionProto.Action action)
	{
		new ProtoAction(seat, action).handle(actionHandler);
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
	public void onAddedToGame(ActionHandler actionHandler, PlayerSeat seat)
	{
		this.actionHandler = actionHandler;
		this.seat = seat;
	}

	@Override
	public void onRemovedFromGame()
	{
		actionHandler = null;
		seat = null;
	}
}
