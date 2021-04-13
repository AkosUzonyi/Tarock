package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.stream.*;

@Service
public class GameSessionService
{
	@Autowired
	private GameSessionRepository gameSessionRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private GameService gameService;

	public PlayerDB getPlayerFromUser(GameSessionDB gameSessionDB, int userId)
	{
		return gameSessionDB.players.stream().filter(p -> p.user.id == userId).findFirst().orElse(null);
	}

	public GameSessionDB findGameSession(int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameSessionId).orElse(null);
		if (gameSessionDB == null || gameSessionDB.state.equals("deleted"))
			throw new NotFoundException();

		return gameSessionDB;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public int createGameSession(GameType type, DoubleRoundType doubleRoundType, int creatorUserId)
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.type = type.getID();
		gameSessionDB.state = "lobby";
		gameSessionDB.players = new ArrayList<>();
		gameSessionDB.doubleRoundType = doubleRoundType.getID();
		gameSessionDB.doubleRoundData = 0;
		gameSessionDB.currentGameId = null;
		gameSessionDB.createTime = System.currentTimeMillis();

		gameSessionDB = gameSessionRepository.save(gameSessionDB);

		PlayerDB creatorPlayer = new PlayerDB();
		creatorPlayer.gameSession = gameSessionDB;
		creatorPlayer.ordinal = 0;
		creatorPlayer.user = userRepository.findById(creatorUserId).orElseThrow();
		creatorPlayer.points = 0;
		gameSessionDB.players.add(creatorPlayer);

		gameSessionDB = gameSessionRepository.save(gameSessionDB);

		return gameSessionDB.id;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void deleteGameSession(int gameSessionId)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameSessionId).orElse(null);
		if (gameSessionDB == null || gameSessionDB.state.equals("deleted"))
			return;

		if (gameSessionDB.state.equals("lobby"))
			gameSessionDB.players.clear();

		gameSessionDB.currentGameId = null;
		gameSessionDB.state = "deleted";
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void joinGameSession(int gameSessionId, int userId)
	{
		GameSessionDB gameSessionDB = findGameSession(gameSessionId);

		if (!gameSessionDB.state.equals("lobby") || getPlayerFromUser(gameSessionDB, userId) != null)
			return;

		PlayerDB player = new PlayerDB();
		player.gameSession = gameSessionDB;
		player.ordinal = gameSessionDB.players.size();
		player.points = 0;
		player.user = userRepository.findById(userId).orElseThrow();
		gameSessionDB.players.add(player);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void leaveGameSession(int gameSessionId, int userId)
	{
		GameSessionDB gameSessionDB = findGameSession(gameSessionId);

		if (!gameSessionDB.state.equals("lobby"))
			return;

		List<UserDB> users = gameSessionDB.players.stream().map(p -> p.user).collect(Collectors.toList());
		users.removeIf(user -> user.id == userId);

		for (int i = 0; i < users.size(); i++)
			gameSessionDB.players.get(i).user = users.get(i);

		while (gameSessionDB.players.size() > users.size())
			gameSessionDB.players.remove(users.size());

		if (gameSessionDB.players.isEmpty())
			gameSessionDB.state = "deleted";
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void startGameSession(int gameSessionId)
	{
		GameSessionDB gameSessionDB = findGameSession(gameSessionId);

		int playerCount = gameSessionDB.players.size();
		while (playerCount < 4)
		{
			PlayerDB bot = new PlayerDB();
			bot.gameSession = gameSessionDB;
			bot.ordinal = playerCount++;
			bot.user = userRepository.findById(4 - playerCount + 1).orElseThrow();
			bot.points = 0;
			gameSessionDB.players.add(bot);
		}

		List<UserDB> users = gameSessionDB.players.stream().map(p -> p.user).collect(Collectors.toList());
		Collections.shuffle(users);
		for (int i = 0; i < playerCount; i++)
			gameSessionDB.players.get(i).user = users.get(i);

		gameSessionDB.state = "game";
		gameService.startNewGame(gameSessionId, 0);
	}
}
