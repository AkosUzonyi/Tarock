package com.tisza.tarock.api.model;

import com.tisza.tarock.game.*;

import java.util.*;

public class GameSession
{
	public int id;
	public String type;
	public String state;
	public String doubleRoundType;
	public List<Player> players;
	public Integer currentGameId;
	public long createTime;

	public int getId()
	{
		return id;
	}

	public GameType getType()
	{
		return GameType.fromID(type);
	}

	public GameSessionState getState()
	{
		return GameSessionState.fromId(state);
	}

	public String getDoubleRoundType()
	{
		return doubleRoundType;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public Integer getCurrentGameId()
	{
		return currentGameId;
	}

	public long getCreateTime()
	{
		return createTime;
	}

	public boolean containsUser(int userID)
	{
		for (Player player : players)
			if (player.user.id == userID)
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
		if (!(obj instanceof GameSession))
			return false;

		return id == ((GameSession)obj).id;
	}
}
