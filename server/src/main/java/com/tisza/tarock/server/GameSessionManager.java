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
	private Map<Integer, Collection<User>> gameIDTokibices = new HashMap<>();

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
		gameIDTokibices.put(id, new HashSet<>());

		System.out.println("game created with id: " + id);

		return id;
	}

	public boolean isGameOwnedBy(int id, User user)
	{
		return gameIDToUsers.get(id).contains(user);
	}

	public void deleteGame(int id)
	{
		GameSession game = games.remove(id);
		List<User> users = gameIDToUsers.remove(id);
		Collection<User> kibices = gameIDTokibices.remove(id);

		for (User user : users)
		{
			user.removePlayerForGame(id);
		}

		for (User kibic : kibices)
		{
			kibic.removePlayerForGame(id);
		}

		game.stopSession();
	}

	public ProtoPlayer addKibic(User user, int gameID)
	{
		ProtoPlayer player = user.createPlayerForGame(gameID);
		games.get(gameID).addKibic(player);
		gameIDTokibices.get(gameID).add(user);
		return player;
	}

	public void removeKibic(User user, int gameID)
	{
		games.get(gameID).removeKibic(user.getPlayerForGame(gameID));
		user.removePlayerForGame(gameID);
		gameIDTokibices.get(gameID).remove(user);
	}

	public void shutdown()
	{
		for (int gameID : games.keySet())
		{
			deleteGame(gameID);
		}
	}
}
