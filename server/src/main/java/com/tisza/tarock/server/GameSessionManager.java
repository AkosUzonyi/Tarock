package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;
import io.reactivex.Observable;
import io.reactivex.*;

import java.util.*;
import java.util.concurrent.*;

public class GameSessionManager
{
	private final TarockDatabase database;
	private final ScheduledExecutorService gameExecutorService;

	private Map<Integer, GameSession> games = new HashMap<>();

	public GameSessionManager(TarockDatabase database, ScheduledExecutorService gameExecutorService)
	{
		this.gameExecutorService = gameExecutorService;
		this.database = database;
	}

	public List<String> getPlayerNames(int gameID)
	{
		return games.get(gameID).getPlayerNames();
	}

	public Collection<GameInfo> listGames()
	{
		Collection<GameInfo> result = new ArrayList<>();

		for (int id : games.keySet())
		{
			GameSession game = games.get(id);
			result.add(new GameInfo(id, game.getGameType(), game.getPlayerNames()));
		}

		return result;
	}

	public Single<Integer> createNewGame(GameType type, List<User> users, DoubleRoundType doubleRoundType)
	{
		if (users.size() != 4)
			throw new IllegalArgumentException("users.size() != 4: " + users.size());

		return Observable.fromIterable(users).flatMapSingle(user -> user.createPlayer(gameExecutorService)).toList().flatMap(players ->
		{
			Collections.shuffle(players);

			return GameSession.create(type, players, doubleRoundType, database).map(game ->
			{
				games.put(game.getID(), game);

				for (PlayerSeat seat : PlayerSeat.getAll())
				{
					Player player = players.get(seat.asInt());
					database.addPlayer(game.getID(), seat, player.getUser());
				}

				game.startSession();

				System.out.println("game session created: users: " + users + " id: " + game.getID());

				return game.getID();
			});
		});

	}

	public Player getPlayer(int gameID, User user)
	{
		for (Player player : games.get(gameID).getPlayers().values())
			if (player.getUser().equals(user))
				return player;

		return null;
	}

	public boolean isGameOwnedBy(int gameID, User user)
	{
		return getPlayer(gameID, user) != null;
	}

	public void deleteGame(int id)
	{
		GameSession game = games.remove(id);
		game.stopSession();
		System.out.println("game session deleted: id = " + id);
	}

	public Single<Player> addKibic(int gameID, User user)
	{
		if (!games.containsKey(gameID))
			return null;

		return user.createPlayer(gameExecutorService).doOnSuccess(player -> games.get(gameID).addKibic(player));
	}

	public void removeKibic(int gameID, Player player)
	{
		games.get(gameID).removeKibic(player);
	}

	public void shutdown()
	{
		for (int gameID : games.keySet())
		{
			deleteGame(gameID);
		}
	}
}
