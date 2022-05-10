package com.tisza.tarock.game.phase;


import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.apache.catalina.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameSessionServiceTest
{
	private static final int GAME_SESSION_ID = 42;
	private static final int GAME_ID = 43;

	private GameSessionRepository gameSessionRepositoryMock;
	private GameService gameServiceMock;
	private UserRepository userRepositoryMock;
	private PlayerRepository playerRepositoryMock;

	private GameSessionService gameSessionService;

	@BeforeEach
	public void before()
	{
		gameSessionRepositoryMock = mock(GameSessionRepository.class);
		gameServiceMock = mock(GameService.class);
		userRepositoryMock = mock(UserRepository.class);
		playerRepositoryMock = mock(PlayerRepository.class);
		gameSessionService = new GameSessionService(gameSessionRepositoryMock, userRepositoryMock, playerRepositoryMock, gameServiceMock);
	}

	@Test
	public void deleteGameSession()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "game";
		gameSessionDB.currentGameId = GAME_ID;

		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));

		gameSessionService.deleteGameSession(GAME_SESSION_ID);

		assertNull(gameSessionDB.currentGameId);
		assertEquals("deleted", gameSessionDB.state);
	}

	@Test
	public void joinGameSession_lobby()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "lobby";
		gameSessionDB.currentGameId = GAME_ID;
		gameSessionDB.players = new ArrayList<>();
		int playerCount = 2;
		for (int i = 0; i < playerCount; i++)
		{
			UserDB userDB = new UserDB();
			userDB.id = i;

			PlayerDB playerDB = new PlayerDB();
			playerDB.gameSession = gameSessionDB;
			playerDB.ordinal = i;
			playerDB.user = userDB;
			gameSessionDB.players.add(playerDB);
		}
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));

		UserDB newUser = new UserDB();
		newUser.id = playerCount;
		when(userRepositoryMock.findById(eq(newUser.id))).thenReturn(Optional.of(newUser));

		gameSessionService.joinGameSession(GAME_SESSION_ID, newUser.id);

		assertEquals(playerCount + 1, gameSessionDB.players.size());
		assertEquals(newUser, gameSessionDB.players.get(playerCount).user);
		assertEquals(playerCount, gameSessionDB.players.get(playerCount).ordinal);
	}

	@Test
	public void joinGameSession_game()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "game";
		gameSessionDB.currentGameId = GAME_ID;
		gameSessionDB.players = new ArrayList<>();
		int playerCount = 0;
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));

		UserDB newUser = new UserDB();
		newUser.id = playerCount;
		when(userRepositoryMock.findById(eq(newUser.id))).thenReturn(Optional.of(newUser));

		gameSessionService.joinGameSession(GAME_SESSION_ID, newUser.id);

		assertEquals(0, gameSessionDB.players.size());
	}

	@Test
	public void leaveGameSession_lobby()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "lobby";
		gameSessionDB.currentGameId = GAME_ID;
		gameSessionDB.players = new ArrayList<>();
		int playerCount = 3;
		for (int i = 0; i < playerCount; i++)
		{
			UserDB userDB = new UserDB();
			userDB.id = i;

			PlayerDB playerDB = new PlayerDB();
			playerDB.gameSession = gameSessionDB;
			playerDB.ordinal = i;
			playerDB.user = userDB;
			gameSessionDB.players.add(playerDB);
		}
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));

		gameSessionService.leaveGameSession(GAME_SESSION_ID, 1);

		assertEquals(playerCount - 1, gameSessionDB.players.size());
		assertEquals(0, gameSessionDB.players.get(0).ordinal);
		assertEquals(1, gameSessionDB.players.get(1).ordinal);
		assertEquals(0, gameSessionDB.players.get(0).user.id);
		assertEquals(2, gameSessionDB.players.get(1).user.id);
	}

	@Test
	public void startGameSession()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "lobby";
		gameSessionDB.currentGameId = GAME_ID;
		gameSessionDB.players = new ArrayList<>();
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));
		when(userRepositoryMock.findById(any())).thenReturn(Optional.of(new UserDB()));

		gameSessionService.startGameSession(GAME_SESSION_ID);

		verify(gameServiceMock, times(1)).startNewGame(eq(GAME_SESSION_ID), eq(0));
		assertEquals(4, gameSessionDB.players.size());
		assertEquals("game", gameSessionDB.state);
	}
}

