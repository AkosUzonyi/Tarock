package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;

public class GameSessionManager
{
	private final File saveDir;
	private final BotFactory botFactory;

	private int nextID = 0;
	private Map<Integer, GameSession> games = new HashMap<>();
	private Map<Integer, List<User>> gameIDToUsers = new HashMap<>();

	public GameSessionManager(File dataDir, BotFactory botFactory)
	{
		this.botFactory = botFactory;

		saveDir = new File(dataDir, "games");
		saveDir.mkdir();
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

	public int createNewGame(GameType type, List<User> users, DoubleRoundType doubleRoundType)
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
			players.add(botFactory.createBot(i));
		}

		Collections.shuffle(players);

		GameSession game = new GameSession(type, players, doubleRoundType, saveDir);
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
