package com.tisza.tarock.server.player.bot;

import com.tisza.tarock.message.*;
import com.tisza.tarock.server.database.*;

import java.util.*;

public class TestBotPlayer extends BotPlayer
{

	private final List<Action> actions = new ArrayList<>();

	public TestBotPlayer()
	{
		this(new User(-1, new TarockDatabase()));
	}

	public TestBotPlayer(User user)
	{
		super(user, 0, 0);
	}

	public List<Action> getActions()
	{
		return actions;
	}

	@Override
	protected void enqueueActionDelayed(Action action, int delayMillis)
	{
		actions.add(action);
	}
}
