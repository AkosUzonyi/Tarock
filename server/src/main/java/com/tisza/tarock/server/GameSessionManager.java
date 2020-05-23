package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.server.database.*;
import io.reactivex.*;

import java.util.*;

public class GameSessionManager
{
	private static final int MAX_GAME_IDLE_TIME = 2 * 3600 * 1000;

	private final Server server;
	private Map<Integer, GameSession> gameSessions = new HashMap<>();
	private List<User> bots = new ArrayList<>();

	public GameSessionManager(Server server)
	{
		this.server = server;
	}

	public void initialize()
	{
		server.getDatabase().getActiveGameSessionIDs()
				.flatMapSingle(id -> GameSession.load(id, server.getDatabase()))
				.doOnNext(gameSession -> gameSessions.put(gameSession.getID(), gameSession))
				.ignoreElements().blockingAwait();

		server.getDatabase().getBotUsers()
				.doOnNext(botUser -> bots.add(botUser))
				.ignoreElements().blockingAwait();
	}

	public Single<GameSession> createGameSession(GameType gameType, DoubleRoundType doubleRoundType, User creator)
	{
		return
		GameSession.createNew(gameType, doubleRoundType, server.getDatabase()).doOnSuccess(gameSession ->
		{
			gameSession.addPlayer(creator.createPlayer());
			gameSessions.put(gameSession.getID(), gameSession);
			server.broadcastStatus();
		});
	}

	public void startGameSessionLobbyWithBots(int id)
	{
		GameSession gameSession = gameSessions.get(id);
		if (gameSession.getState() != GameSession.State.LOBBY)
			throw new IllegalStateException("GameSession already started");

		for (User bot : bots)
			if (gameSession.getFreeLobbyPlaces() > 0)
				gameSession.addPlayer(bot.createPlayer());

		gameSession.start();
		server.broadcastStatus();
	}

	public Single<GameSession> createGameSessionHistoryView(int gameID)
	{
		int gameSessionID = -1;
		while (gameSessions.containsKey(gameSessionID))
			gameSessionID--;

		return
		GameSession.createHistoryView(gameID, gameSessionID, server.getDatabase()).doOnSuccess(gameSession ->
		{
			gameSessions.put(gameSession.getID(), gameSession);
			server.broadcastStatus();
		});
	}

	public GameSession getGameSession(int id)
	{
		return gameSessions.get(id);
	}

	public Collection<GameSession> getGameSessions()
	{
		return gameSessions.values();
	}

	public void deleteOldGames()
	{
		for (GameSession gameSession : gameSessions.values())
			if (gameSession.getLastModified() < System.currentTimeMillis() - MAX_GAME_IDLE_TIME)
				gameSession.endSession();

		gameSessions.entrySet().removeIf(entry -> entry.getValue().getState() == GameSession.State.ENDED);

		server.broadcastStatus();
	}

	public void shutdown()
	{
	}
}
