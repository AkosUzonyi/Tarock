package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;

public class ProtoPlayer implements Player
{
	private String name;
	private ProtoEventHandler eventHandler = new ProtoEventHandler();
	private Game game;
	private PlayerSeat seat;

	public ProtoPlayer(String name)
	{
		this.name = name;
	}

	public void useConnection(ProtoConnection connection)
	{
		eventHandler.useConnection(connection);

		if (connection != null)
		{
			game.requestHistory(seat, eventHandler);
		}
	}

	public void queueAction(String action)
	{
		if (seat != null)
			game.action(new Action(seat, action));
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public EventHandler getEventHandler()
	{
		return eventHandler;
	}

	@Override
	public void setGame(Game game, PlayerSeat seat)
	{
		this.game = game;
		this.seat = seat;
	}

}
