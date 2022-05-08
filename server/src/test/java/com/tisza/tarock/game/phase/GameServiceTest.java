package com.tisza.tarock.game.phase;


import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import com.tisza.tarock.spring.service.*;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameServiceTest
{
	private static final int GAME_SESSION_ID = 42;
	private static final int GAME_ID = 43;

	private GameRepository gameRepositoryMock;
	private BotService botServiceMock;
	private GameSessionRepository gameSessionRepositoryMock;
	private DeferredResultService<List<ActionDB>> actionDeferredResultServiceMock;

	private GameService gameService;

	@BeforeEach
	public void before()
	{
		gameRepositoryMock = mock(GameRepository.class);
		botServiceMock = mock(BotService.class);
		gameSessionRepositoryMock = mock(GameSessionRepository.class);
		actionDeferredResultServiceMock = mock(DeferredResultService.class);
		gameService = new GameService(gameRepositoryMock, botServiceMock, gameSessionRepositoryMock, actionDeferredResultServiceMock);
	}

	@Test
	public void startNewGame_gameSessionNotExists()
	{
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.empty());
		assertThrows(NotFoundException.class, () -> gameService.startNewGame(GAME_SESSION_ID, 0));
	}

	@Test
	public void startNewGame_gameSessionDeleted()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.state = "deleted";
		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));
		assertThrows(IllegalStateException.class, () -> gameService.startNewGame(GAME_SESSION_ID, 0));

	}

	@Test
	public void startNewGame_successful()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "game";
		gameSessionDB.currentGameId = 0;

		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));
		when(gameRepositoryMock.save(any())).thenAnswer(invocationOnMock ->
		{
			GameDB gameDB = invocationOnMock.getArgument(0, GameDB.class);
			gameDB.id = GAME_ID;
			return gameDB;
		});

		gameService.startNewGame(GAME_SESSION_ID, 0);

		verify(gameRepositoryMock, times(1)).save(any());
		assertEquals(GAME_ID, gameSessionDB.currentGameId);
	}

	@Test
	public void executeAction_successful()
	{
		GameSessionDB gameSessionDB = new GameSessionDB();
		gameSessionDB.id = GAME_SESSION_ID;
		gameSessionDB.type = "paskievics";
		gameSessionDB.state = "game";
		gameSessionDB.currentGameId = GAME_ID;

		GameDB gameDB = new GameDB();
		gameDB.id = GAME_ID;
		gameDB.gameSession = gameSessionDB;
		gameDB.beginnerPlayer = 0;
		gameDB.actions = new ArrayList<>();
		gameDB.deckCards = new ArrayList<>();
		List<Card> deck = new ArrayList<>(Card.getAll());
		for (int i = 0; i < Card.getAll().size(); i++)
		{
			DeckCardDB deckCardDB = new DeckCardDB();
			deckCardDB.card = deck.get(i).getID();
			deckCardDB.game = gameDB;
			deckCardDB.ordinal = i;
			gameDB.deckCards.add(deckCardDB);
		}

		when(gameSessionRepositoryMock.findById(eq(GAME_SESSION_ID))).thenReturn(Optional.of(gameSessionDB));
		when(gameRepositoryMock.findById(eq(GAME_ID))).thenReturn(Optional.of(gameDB));

		assertFalse(gameService.executeAction(GAME_ID, PlayerSeat.SEAT1, Action.announcePassz()));
		assertFalse(gameService.executeAction(GAME_ID, PlayerSeat.SEAT3, Action.throwCards()));
		assertTrue(gameService.executeAction(GAME_ID, PlayerSeat.SEAT0, Action.throwCards()));

		assertEquals(1, gameDB.actions.size());
		assertEquals(Action.throwCards().getId(), gameDB.actions.get(0).action);
		assertEquals(0, gameDB.actions.get(0).seat);
		assertEquals(0, gameDB.actions.get(0).ordinal);
	}
}

