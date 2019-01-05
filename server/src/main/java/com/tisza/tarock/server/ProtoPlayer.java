package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

public class ProtoPlayer implements Player
{
	private String name;
	private ProtoEventSender eventSender = new ProtoEventSender();
	private Game game;
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
			game.requestHistory(seat, eventSender);
		}
	}

	public void queueAction(String action)
	{
		game.action(new Action(seat, action));
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
	public void setGame(Game game, PlayerSeat seat)
	{
		this.game = game;
		this.seat = seat;
	}

}
