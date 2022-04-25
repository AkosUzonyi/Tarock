package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Service
public class GameService
{
	private final GameRepository gameRepository;
	private final BotService botService;
	private final GameSessionRepository gameSessionRepository;
	private final DeferredResultService<List<ActionDB>> actionDeferredResultService;

	public GameService(GameRepository gameRepository, BotService botService, GameSessionRepository gameSessionRepository, DeferredResultService<List<ActionDB>> actionDeferredResultService)
	{
		this.gameRepository = gameRepository;
		this.botService = botService;
		this.gameSessionRepository = gameSessionRepository;
		this.actionDeferredResultService = actionDeferredResultService;
	}

	private final ScheduledExecutorService taskScheduler = Executors.newSingleThreadScheduledExecutor();

	public PlayerDB getPlayerFromSeat(GameDB gameDB, PlayerSeat seat)
	{
		return gameDB.gameSession.players.get((gameDB.beginnerPlayer + seat.asInt()) % gameDB.gameSession.players.size());
	}

	public PlayerSeat getSeatFromPlayer(GameDB gameDB, PlayerDB playerDB)
	{
		int playerCount = gameDB.gameSession.players.size();
		int seat = (playerDB.ordinal - gameDB.beginnerPlayer + playerCount) % playerCount;
		if (seat < 0 || seat >= PlayerSeat.getAll().length)
			return null;

		return PlayerSeat.fromInt(seat);
	}

	public GameDB findGame(int gameId)
	{
		GameDB gameDB = gameRepository.findById(gameId).orElseThrow(NotFoundException::new);
		if (gameDB.gameSession.state.equals("deleted") || gameDB.gameSession.currentGameId != gameDB.id)
			throw new GoneException();

		return gameDB;
	}

	public Game loadGame(int gameId)
	{
		return loadGame(gameId, false);
	}

	private Game loadGame(int gameId, boolean future)
	{
		GameDB gameDB = findGame(gameId);

		List<Card> deck = gameDB.deckCards.stream().map(deckCardDB -> Card.fromId(deckCardDB.card)).collect(Collectors.toList());
		Game game = new Game(GameType.fromID(gameDB.gameSession.type), deck);

		long now = System.currentTimeMillis();
		for (ActionDB actionDB : gameDB.actions)
			if (future || actionDB.time < now)
				game.processAction(PlayerSeat.fromInt(actionDB.seat), new Action(actionDB.action));

		return game;
	}

	public List<ActionDB> getActionsFiltered(int gameId)
	{
		GameDB gameDB = findGame(gameId);
		List<ActionDB> filteredActions = new ArrayList<>();
		long now = System.currentTimeMillis();
		for (ActionDB actionDB : gameDB.actions)
		{
			if (actionDB.time > now)
				continue;

			ActionDB newActionDB = new ActionDB();
			newActionDB.game = actionDB.game;
			newActionDB.ordinal = actionDB.ordinal;
			newActionDB.seat = actionDB.seat;
			newActionDB.action = actionDB.action.startsWith("fold:") ? "fold:" : actionDB.action;
			newActionDB.time = actionDB.time;
			filteredActions.add(newActionDB);
		}
		return filteredActions;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void startNewGame(int gameSessionId, int beginnerPlayer)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameSessionId).orElseThrow(NotFoundException::new);

		if (!gameSessionDB.state.equals("game"))
			throw new IllegalStateException();

		GameDB gameDB = new GameDB();
		gameDB.gameSession = gameSessionDB;
		gameDB.actions = new ArrayList<>();
		gameDB.beginnerPlayer = beginnerPlayer;
		gameDB.createTime = System.currentTimeMillis();
		gameDB = gameRepository.save(gameDB);

		gameDB.gameSession.currentGameId = gameDB.id;

		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);
		List<DeckCardDB> deckDB = new ArrayList<>();
		for (Card card : deck)
		{
			DeckCardDB deckCardDB = new DeckCardDB();
			deckCardDB.card = card.getID();
			deckCardDB.game = gameDB;
			deckCardDB.ordinal = deckDB.size();
			deckDB.add(deckCardDB);
		}

		gameDB.deckCards = deckDB;

		Game game = new Game(GameType.fromID(gameDB.gameSession.type), deck);

		botService.executeBotActions(this, gameDB, game, 0);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean executeAction(int gameId, PlayerSeat seat, Action action)
	{
		return executeAction(gameId, seat, action, 0);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean executeAction(int gameId, PlayerSeat seat, Action action, int delay)
	{
		GameDB gameDB = findGame(gameId);
		Game game = loadGame(gameId, true);

		boolean success = game.processAction(seat, action);
		if (!success)
			return false;

		ActionDB actionDB = new ActionDB();
		actionDB.game = gameDB;
		actionDB.ordinal = gameDB.actions.size();
		actionDB.seat = seat.asInt();
		actionDB.action = action.getId();
		actionDB.time = System.currentTimeMillis() + delay;
		gameDB.actions.add(actionDB);
		if (delay <= 0)
			actionDeferredResultService.notifyNewResult(gameDB.id);
		else
			taskScheduler.schedule(() -> actionDeferredResultService.notifyNewResult(gameDB.id), delay + 50, TimeUnit.MILLISECONDS);

		botService.executeBotActions(this, gameDB, game, delay);

		if (game.isFinished())
		{
			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameDB.gameSession.doubleRoundType));
			doubleRoundTracker.setData(gameDB.gameSession.doubleRoundData);

			for (PlayerSeat s : PlayerSeat.getAll())
			{
				PlayerDB p = getPlayerFromSeat(gameDB, s);
				p.points += game.getPoints(s) * doubleRoundTracker.getCurrentMultiplier();
			}

			if (game.isNormalFinish())
				doubleRoundTracker.gameFinished();
			else
				doubleRoundTracker.gameInterrupted();
			gameDB.gameSession.doubleRoundData = doubleRoundTracker.getData();

			startNewGame(gameDB.gameSession.id, (gameDB.beginnerPlayer + 1) % gameDB.gameSession.players.size());
		}

		return true;
	}
}
