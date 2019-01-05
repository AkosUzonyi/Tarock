package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

public class TestPlayer implements Player
{
	private final String name;
	private Game game;
	private PlayerSeat seat;

	public TestPlayer(String name)
	{
		this.name = name;
	}

	public void bid(int bid)
	{
		game.action(Action.bid(seat, bid));
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
	public void setGame(Game game, PlayerSeat seat)
	{
		this.game = game;
		this.seat = seat;
	}

}
