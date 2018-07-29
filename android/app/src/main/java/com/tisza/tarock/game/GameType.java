package com.tisza.tarock.game;

public enum GameType
{
	PASKIEVICS(), ILLUSZTRALT(PASKIEVICS), MAGAS(ILLUSZTRALT), ZEBI(MAGAS);

	private final GameType[] parents;

	GameType(GameType ... parents)
	{
		this.parents = parents;
	}

	public boolean hasParent(GameType gameType)
	{
		if (this == gameType)
			return true;

		for (GameType parent : parents)
		{
			if (parent.hasParent(gameType))
			{
				return true;
			}
		}

		return false;
	}
}
