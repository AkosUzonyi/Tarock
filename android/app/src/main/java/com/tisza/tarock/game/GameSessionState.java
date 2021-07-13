package com.tisza.tarock.game;

public enum GameSessionState
{
	LOBBY, GAME, DELETED;

	public static GameSessionState fromId(String id)
	{
		switch (id)
		{
			case "lobby": return GameSessionState.LOBBY;
			case "game": return GameSessionState.GAME;
			case "deleted": return GameSessionState.DELETED;
		}
		throw new RuntimeException();
	}
}
