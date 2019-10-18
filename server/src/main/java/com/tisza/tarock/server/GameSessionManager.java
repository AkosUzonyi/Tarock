package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.message.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class GameSessionManager
{
	private final File saveDir;
	private final ScheduledExecutorService gameExecutorService;

	private int nextID = 0;
	private Map<Integer, GameSession> games = new HashMap<>();
	private Map<Integer, Map<User, ProtoPlayer>> gameIDAndUserToPlayers = new HashMap<>();

	public GameSessionManager(File dataDir, ScheduledExecutorService gameExecutorService)
	{
		this.gameExecutorService = gameExecutorService;

		saveDir = new File(dataDir, "games");
		saveDir.mkdir();
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

	public int createNewGame(GameType type, List<User> users, DoubleRoundType doubleRoundType)
	{
		if (users.size() > 4)
			throw new IllegalArgumentException("users.size() > 4: " + users.size());

		int id = nextID++;

		List<User> shuffledUsers = new ArrayList<>(users);
		int randomPlayerCount = 4 - users.size();
		for (int i = 0; i < randomPlayerCount; i++)
			shuffledUsers.add(null);
		Collections.shuffle(shuffledUsers);

		Map<User, ProtoPlayer> userToPlayers = new HashMap<>();
		List<Player> players = new ArrayList<>();
		int bot = 0;
		for (User user : shuffledUsers)
		{
			if (user == null)
			{
				players.add(new RandomPlayer("bot" + bot++, gameExecutorService, 500, 2000));
			}
			else
			{
				ProtoPlayer player = new ProtoPlayer(user.getName());
				players.add(player);
				userToPlayers.put(user, player);
			}
		}
		gameIDAndUserToPlayers.put(id, userToPlayers);

		GameSession game = new GameSession(type, players, doubleRoundType, saveDir);
		games.put(id, game);
		game.startSession();

		System.out.println("game session created: users: " + users + " id: " + id);

		return id;
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

	public ProtoPlayer addKibic(int gameID, User user)
	{
		if (!games.containsKey(gameID))
			return null;

		ProtoPlayer player = new ProtoPlayer(user.getName());
		games.get(gameID).addKibic(player);
		return player;
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
