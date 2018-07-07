package com.tisza.tarock;

import java.util.*;

public class GameInfo
{
	private final int id;
	private final GameType type;
	private final List<String> playerNames;

	public GameInfo(int id, GameType type, List<String> playerNames)
	{
		this.id = id;
		this.type = type;
		this.playerNames = playerNames;
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
}
