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

	public String getID()
	{
		switch (this)
		{
			case PASKIEVICS: return "paskievics";
			case ILLUSZTRALT: return "illusztralt";
			case MAGAS: return "magas";
			case ZEBI: return "zebi";
		}
		throw new RuntimeException();
	}

	public static GameType fromID(String id)
	{
		switch (id)
		{
			case "paskievics": return PASKIEVICS;
			case "illusztralt": return ILLUSZTRALT;
			case "magas": return MAGAS;
			case "zebi": return ZEBI;
		}
		throw new IllegalArgumentException("invalid game type: " + id);
	}
}
