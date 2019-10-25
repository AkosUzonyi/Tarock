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
	private Map<Integer, Map<User, ProtoPlayer>> gameIDAndUserToPlayers = new HashMap<>();

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
		if (users.size() > 4)
			throw new IllegalArgumentException("users.size() > 4: " + users.size());

		return Observable.fromIterable(users).flatMapSingle(ProtoPlayer::createFromUser).toList().flatMap(protoPlayers ->
		{
			List<Player> players = new ArrayList<>();
			players.addAll(protoPlayers);
			int bot = 0;
			for (int i = protoPlayers.size(); i < 4; i++)
				players.add(new RandomPlayer("bot" + bot++, gameExecutorService, 500, 2000));

			Collections.shuffle(players);

			return GameSession.create(type, players, doubleRoundType, database).map(game ->
			{
				Map<User, ProtoPlayer> userToPlayers = new HashMap<>();
				for (ProtoPlayer protoPlayer : protoPlayers)
					userToPlayers.put(protoPlayer.getUser(), protoPlayer);

				games.put(game.getID(), game);
				gameIDAndUserToPlayers.put(game.getID(), userToPlayers);
				for (int i = 0; i < 4; i++)
					if (players.get(i) instanceof ProtoPlayer)
						database.addUserPlayer(game.getID(), i, ((ProtoPlayer)players.get(i)).getUser().getID());
					else
						database.addBotPlayer(game.getID(), i);

				game.startSession();

				System.out.println("game session created: users: " + users + " id: " + game.getID());

				return game.getID();
			});
		});

	}

	public ProtoPlayer getPlayer(int gameID, User user)
	{
		return gameIDAndUserToPlayers.get(gameID).get(user);
	}

	public boolean isGameOwnedBy(int id, User user)
	{
		return gameIDAndUserToPlayers.get(id).containsKey(user);
	}

	public void deleteGame(int id)
	{
		GameSession game = games.remove(id);
		gameIDAndUserToPlayers.remove(id);
		game.stopSession();
		System.out.println("game session deleted: id = " + id);
	}

	public Single<ProtoPlayer> addKibic(int gameID, User user)
	{
		if (!games.containsKey(gameID))
			return null;

		return ProtoPlayer.createFromUser(user).doOnSuccess(player -> games.get(gameID).addKibic(player));
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
