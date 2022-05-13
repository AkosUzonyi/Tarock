package com.tisza.tarock.game.phase.util;

import com.tisza.tarock.message.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.player.bot.*;

import java.util.*;

public class ExtendedBotPlayer extends BotPlayer
{

	private final List<Action> actions = new ArrayList<>();

	public ExtendedBotPlayer()
	{
		this(new User(-1, new TarockDatabase()));
	}

	public ExtendedBotPlayer(User user)
	{
		super(user, 0, 0);
	}

	public List<Action> getActions()
	{
		return actions;
	}

	@Override
	protected void doAction(Action action)
	{
		actions.add(action);
	}
}
