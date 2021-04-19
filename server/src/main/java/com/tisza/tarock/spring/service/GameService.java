package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.spring.exception.*;
import com.tisza.tarock.spring.model.*;
import com.tisza.tarock.spring.repository.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.util.*;
import java.util.stream.*;

@Service
public class GameService
{
	@Autowired
	private GameRepository gameRepository;
	@Autowired
	private BotService botService;
	@Autowired
	private GameSessionRepository gameSessionRepository;
	@Autowired
	private ListDeferredResultService<ActionDB> actionDeferredResultService;

	public PlayerDB getPlayerFromSeat(GameDB gameDB, PlayerSeat seat)
	{
		return gameDB.gameSession.players.get((gameDB.beginnerPlayer + seat.asInt()) % gameDB.gameSession.players.size());
	}

	public PlayerSeat getSeatFromPlayer(GameDB gameDB, PlayerDB playerDB)
	{
		int playerCount = gameDB.gameSession.players.size();
		return PlayerSeat.fromInt((playerDB.ordinal - gameDB.beginnerPlayer + playerCount) % playerCount);
	}

	public GameDB findGame(int gameId)
	{
		return gameRepository.findById(gameId).orElseThrow(NotFoundException::new);
	}

	public Game loadGame(int gameId)
	{
		GameDB gameDB = findGame(gameId);

		List<Card> deck = gameDB.deckCards.stream().map(deckCardDB -> Card.fromId(deckCardDB.card)).collect(Collectors.toList());
		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameDB.gameSession.doubleRoundType));
		doubleRoundTracker.setData(gameDB.gameSession.doubleRoundData);
		Game game = new Game(GameType.fromID(gameDB.gameSession.type), deck, doubleRoundTracker.getCurrentMultiplier());
		game.start();
		for (ActionDB a : gameDB.actions)
			game.processAction(PlayerSeat.fromInt(a.seat), new Action(a.action));

		botService.executeBotActions(gameDB, game);

		return game;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public List<ActionDB> getActionsFiltered(int gameId)
	{
		GameDB gameDB = findGame(gameId);
		List<ActionDB> filteredActions = new ArrayList<>();
		for (ActionDB actionDB : gameDB.actions)
		{
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

		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameDB.gameSession.doubleRoundType));
		doubleRoundTracker.setData(gameDB.gameSession.doubleRoundData);
		Game game = new Game(GameType.fromID(gameDB.gameSession.type), deck, doubleRoundTracker.getCurrentMultiplier());
		game.start();

		botService.executeBotActions(gameDB, game);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean executeAction(int gameId, PlayerSeat seat, Action action)
	{
		GameDB gameDB = findGame(gameId);
		Game game = loadGame(gameId);

		boolean success = game.processAction(seat, action);
		if (!success)
			return false;

		ActionDB actionDB = new ActionDB();
		actionDB.game = gameDB;
		actionDB.ordinal = gameDB.actions.size();
		actionDB.seat = seat.asInt();
		actionDB.action = action.getId();
		actionDB.time = System.currentTimeMillis();
		gameDB.actions.add(actionDB);
		actionDeferredResultService.notifyNewResult(gameDB.id);

		botService.executeBotActions(gameDB, game);

		if (game.isFinished())
		{
			for (PlayerSeat s : PlayerSeat.getAll())
			{
				PlayerDB p = getPlayerFromSeat(gameDB, s);
				p.points += game.getPoints(s);
			}

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameDB.gameSession.doubleRoundType));
			doubleRoundTracker.setData(gameDB.gameSession.doubleRoundData);
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
