package com.tisza.tarock.game;

public enum GameType
{
	PASKIEVICS, ILLUSZTRALT, MAGAS, ZEBI;

	private GameType[] parents;

	static
	{
		PASKIEVICS.parents = new GameType[]{};
		ILLUSZTRALT.parents = new GameType[]{PASKIEVICS};
		MAGAS.parents = new GameType[]{ILLUSZTRALT};
		ZEBI.parents = new GameType[]{MAGAS};
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
