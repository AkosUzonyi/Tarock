package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.server.*;

public abstract class Player
{
	private User user;
	private String name;

	private PlayerSeat seat;
	private Game game;

	public Player(User user, String name)
	{
		this.user = user;
		this.name = name;
	}

	public abstract void handleEvent(Event event);

	public String getName()
	{
		return name;
	}

	public User getUser()
	{
		return user;
	}

	public PlayerSeat getSeat()
	{
		return seat;
	}

	public void setGame(Game game, PlayerSeat seat)
	{
		this.game = game;
		this.seat = seat;
	}

	protected void doAction(Action action)
	{
		if (game != null && seat != null)
			game.action(seat, action);
	}

	protected void requestHistory()
	{
		if (game != null)
			game.requestHistory(this);
	}
}
