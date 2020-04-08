package com.tisza.tarock.server.player;

import com.tisza.tarock.game.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.message.*;

public abstract class Player
{
	private User user;
	private String name;

	private PlayerSeat seat;
	private GameSession gameSession;

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

	public GameSession getGameSession()
	{
		return gameSession;
	}

	public void setGame(GameSession gameSession, PlayerSeat seat)
	{
		this.gameSession = gameSession;
		this.seat = seat;
	}

	protected void chat(String message)
	{
		if (gameSession != null)
			gameSession.chat(user.getID(), message);
	}

	protected void doAction(Action action)
	{
		if (gameSession != null && seat != null)
			gameSession.action(seat, action);
	}

	protected void requestHistory()
	{
		if (gameSession != null)
			gameSession.requestHistory(this);
	}
}
