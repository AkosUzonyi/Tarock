package com.tisza.tarock.game;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

public class TestPlayer implements Player
{
	private final String name;
	private ActionHandler actionHandler;
	private PlayerSeat seat;

	public TestPlayer(String name)
	{
		this.name = name;
	}

	public void bid(int bid)
	{
		actionHandler.bid(seat, bid);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public EventSender getEventSender()
	{
		return new BroadcastEventSender();
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
	}
}
