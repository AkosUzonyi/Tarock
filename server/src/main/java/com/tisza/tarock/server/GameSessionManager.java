package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class GameSessionManager
{
	private int nextID = 0;
	private Map<Integer, GameSession> games = new HashMap<>();
	private Map<Integer, List<User>> gameIDToUsers = new HashMap<>();

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

	public int createNewGame(GameType type, List<User> users)
	{
		if (users.size() > 4)
			throw new IllegalArgumentException();

		int id = nextID++;

		List<Player> players = new ArrayList<>();

		for (User user : users)
		{
			players.add(user.createPlayerForGame(id));
		}

		int randomPlayerCount = 4 - players.size();
		for (int i = 0; i < randomPlayerCount; i++)
		{
			players.add(new RandomPlayer("bot" + i));
		}

		GameSession game = new GameSession(type, players);
		games.put(id, game);
		game.startSession();

		gameIDToUsers.put(id, users);

		System.out.println("game created with id: " + id);

		return id;
	}

	public boolean hasUserPermissionToDelete(int id, User user)
	{
		return gameIDToUsers.get(id).contains(user);
	}

	public void deleteGame(int id)
	{
		GameSession game = games.remove(id);
		List<User> users = gameIDToUsers.remove(id);

		for (User user : users)
		{
			user.removePlayerForGame(id);
		}

		game.stopSession();
	}

	public void shutdown()
	{
		for (int gameID : games.keySet())
		{
			deleteGame(gameID);
		}
	}
}
