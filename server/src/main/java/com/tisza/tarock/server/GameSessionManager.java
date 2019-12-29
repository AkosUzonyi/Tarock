package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.game.doubleround.*;
import io.reactivex.*;

import java.util.*;

public class GameSessionManager
{
	private static final int MAX_GAME_IDLE_TIME = 6 * 3600 * 1000;

	private final TarockDatabase database;
	private Map<Integer, GameSession> gameSessions = new HashMap<>();

	public GameSessionManager(TarockDatabase database)
	{
		this.database = database;
	}

	public void initialize()
	{
		database.getActiveGameSessionIDs()
				.flatMapSingle(id -> GameSession.load(id, database))
				.doOnNext(gameSession -> gameSessions.put(gameSession.getID(), gameSession))
				.ignoreElements().blockingAwait();
	}

	public Single<GameSession> createGameSession(GameType gameType, List<User> users, DoubleRoundType doubleRoundType)
	{
		return GameSession.createNew(gameType, users, doubleRoundType, database)
				.doOnSuccess(gameSession -> gameSessions.put(gameSession.getID(), gameSession));
	}

	public GameSession getGameSession(int id)
	{
		return gameSessions.get(id);
	}

	public void stopGameSession(int id)
	{
		GameSession gameSession = gameSessions.remove(id);
		gameSession.stopSession();
	}

	public Collection<GameSession> getGameSessions()
	{
		return gameSessions.values();
	}

	public void deleteOldGames()
	{
		for (Map.Entry<Integer, GameSession> gameSessionEntry : new HashSet<>(gameSessions.entrySet()))
			if (gameSessionEntry.getValue().getLastModified() < System.currentTimeMillis() - MAX_GAME_IDLE_TIME)
				stopGameSession(gameSessionEntry.getKey());
	}

	public void shutdown()
	{
	}
}
