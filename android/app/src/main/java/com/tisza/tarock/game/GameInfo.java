package com.tisza.tarock.game;

import java.util.*;

public class GameInfo
{
	private final int id;
	private final GameType type;
	private final List<User> users;
	private final GameSessionState state;

	public GameInfo(int id, GameType type, List<User> users, GameSessionState state)
	{
		this.id = id;
		this.type = type;
		this.users = users;
		this.state = state;
	}

	public int getId()
	{
		return id;
	}

	public GameType getType()
	{
		return type;
	}

	public List<User> getUsers()
	{
		return users;
	}

	public GameSessionState getState()
	{
		return state;
	}

	public boolean containsUser(int userID)
	{
		for (User user : users)
			if (user.getId() == userID)
				return true;

		return false;
	}

	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GameInfo))
			return false;

		return id == ((GameInfo)obj).id;
	}
}
