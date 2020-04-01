package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.server.player.*;
import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.disposables.*;

import java.util.*;

public class GameSessionManager
{
	private static final int MAX_GAME_IDLE_TIME = 3 * 3600 * 1000;

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

	public Single<GameSession> createGameSession(GameType gameType, List<User> users, DoubleRoundType doubleRoundType)
	{
		return
		Observable.fromIterable(users).flatMapSingle(User::createPlayer).toList().flatMap(players ->
		GameSession.createNew(gameType, doubleRoundType, server.getDatabase()).doOnSuccess(gameSession ->
		{
			for (Player player : players)
				gameSession.addPlayer(player);

			if (gameSession.isLobbyFull())
				gameSession.start();

			gameSessions.put(gameSession.getID(), gameSession);
			server.broadcastStatus();
		}));
	}

	public void startGameSessionLobbyWithBots(int id)
	{
		GameSession gameSession = gameSessions.get(id);
		if (gameSession.getState() != GameSession.State.LOBBY)
			throw new IllegalStateException("GameSession already started");

		Observable.fromIterable(bots).flatMapSingle(User::createPlayer).toList().subscribe(players ->
		{
			while (!gameSession.isLobbyFull())
				gameSession.addPlayer(players.remove(0));

			gameSession.start();
			server.broadcastStatus();
		});
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
