package com.tisza.tarock.api.model;

import com.tisza.tarock.game.*;

import java.util.*;

public class GameDTO
{
	public int id;
	public String type;
	public int gameSessionId;
	public List<Player> players = new ArrayList<>();
	public long createTime;

	public int getId()
	{
		return id;
	}

	public GameType getType()
	{
		return GameType.fromID(type);
	}

	public int getGameSessionId()
	{
		return gameSessionId;
	}

	public List<Player> getPlayers()
	{
		return players;
	}

	public long getCreateTime()
	{
		return createTime;
	}
}
