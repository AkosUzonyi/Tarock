package com.tisza.tarock.game;

import java.util.*;

public class GameInfo implements Comparable<GameInfo>
{
	private final int id;
	private final GameType type;
	private final List<String> playerNames;
	private boolean my;

	public GameInfo(int id, GameType type, List<String> playerNames, boolean my)
	{
		this.id = id;
		this.type = type;
		this.playerNames = playerNames;
		this.my = my;
	}

	public int getId()
	{
		return id;
	}

	public GameType getType()
	{
		return type;
	}

	public List<String> getPlayerNames()
	{
		return playerNames;
	}

	public boolean isMy()
	{
		return my;
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

	@Override
	public int compareTo(GameInfo other)
	{
		if (my != other.my)
			return my ? -1 : 1;

		return id - other.id;
	}
}
