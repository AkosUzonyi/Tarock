package com.tisza.tarock.spring.service;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
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
	private GameSessionRepository gameSessionRepository;

	private final Random rnd = new Random();

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Game loadGame(GameDB gameDB)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameDB.gameSessionId).orElseThrow();
		List<Card> deck = gameDB.deckCards.stream().map(deckCardDB -> Card.fromId(deckCardDB.card)).collect(Collectors.toList());
		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameSessionDB.doubleRoundType));
		doubleRoundTracker.setData(gameSessionDB.doubleRoundData);
		Game game = new Game(GameType.fromID(gameSessionDB.type), deck, doubleRoundTracker.getCurrentMultiplier());
		game.start();
		for (ActionDB a : gameDB.actions)
			game.processAction(PlayerSeat.fromInt(a.seat), new Action(a.action));

		executeBotActions(gameSessionDB, gameDB, game);

		return game;
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void startNewGame(GameSessionDB gameSessionDB, int beginnerPlayer)
	{
		if (!gameSessionDB.state.equals("game"))
			throw new IllegalStateException();

		GameDB gameDB = new GameDB();
		gameDB.gameSessionId = gameSessionDB.id;
		gameDB.actions = new ArrayList<>();
		gameDB.beginnerPlayer = beginnerPlayer;
		gameDB.createTime = System.currentTimeMillis();
		gameDB = gameRepository.save(gameDB);

		gameSessionDB.currentGameId = gameDB.id;

		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);
		List<DeckCardDB> deckDB = new ArrayList<>();
		for (Card card : deck)
		{
			DeckCardDB deckCardDB = new DeckCardDB();
			deckCardDB.card = card.getID();
			deckCardDB.gameId = gameDB.id;
			deckCardDB.ordinal = deckDB.size();
			deckDB.add(deckCardDB);
		}

		gameDB.deckCards = deckDB;

		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameSessionDB.doubleRoundType));
		doubleRoundTracker.setData(gameSessionDB.doubleRoundData);
		Game game = new Game(GameType.fromID(gameSessionDB.type), deck, doubleRoundTracker.getCurrentMultiplier());
		game.start();

		executeBotActions(gameSessionDB, gameDB, game);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean executeAction(GameDB gameDB, PlayerSeat seat, Action action)
	{
		GameSessionDB gameSessionDB = gameSessionRepository.findById(gameDB.gameSessionId).orElseThrow();
		Game game = loadGame(gameDB);

		if (!doExecuteAction(gameSessionDB, gameDB, game, seat, action))
			return false;

		executeBotActions(gameSessionDB, gameDB, game);
		return true;
	}

	private boolean doExecuteAction(GameSessionDB gameSessionDB, GameDB gameDB, Game game, PlayerSeat seat, Action action)
	{
		boolean success = game.processAction(seat, action);
		if (!success)
			return false;

		ActionDB actionDB = new ActionDB();
		actionDB.gameId = gameDB.id;
		actionDB.ordinal = gameDB.actions.size();
		actionDB.seat = seat.asInt();
		actionDB.action = action.getId();
		actionDB.time = System.currentTimeMillis();
		gameDB.actions.add(actionDB);

		if (game.isFinished())
		{
			for (PlayerSeat s : PlayerSeat.getAll())
			{
				PlayerDB p = gameSessionDB.players.get((gameDB.beginnerPlayer + s.asInt()) % gameSessionDB.players.size());
				p.points += game.getPoints(s);
			}

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(DoubleRoundType.fromID(gameSessionDB.doubleRoundType));
			doubleRoundTracker.setData(gameSessionDB.doubleRoundData);
			if (game.isNormalFinish())
				doubleRoundTracker.gameFinished();
			else
				doubleRoundTracker.gameInterrupted();
			gameSessionDB.doubleRoundData = doubleRoundTracker.getData();

			startNewGame(gameSessionDB, (gameDB.beginnerPlayer + 1) % gameSessionDB.players.size());
		}

		return true;
	}

	private void executeBotActions(GameSessionDB gameSessionDB, GameDB gameDB, Game game)
	{
		boolean executed;
		do
		{
			executed = false;
			for (PlayerSeat player : PlayerSeat.getAll())
			{
				if (!game.getTurn(player))
					continue;

				PlayerDB playerDB = gameSessionDB.players.get((gameDB.beginnerPlayer + player.asInt()) % gameSessionDB.players.size());
				if (playerDB.user.id >= 4)
					continue;

				//TODO: delay
				doExecuteAction(gameSessionDB, gameDB, game, player, getBotAction(game, player));
				executed = true;
			}
		}
		while (executed);
	}

	private Action getBotAction(Game game, PlayerSeat bot)
	{
		PlayerCards cards = game.getPlayerCards(bot);
		switch (game.getCurrentPhaseEnum())
		{
			case BIDDING:
			case CALLING:
				return chooseRandom(game.getAvailableActions());
			case ANNOUNCING:
				if (!game.getAvailableActions().isEmpty() && rnd.nextFloat() < 0.3)
					return chooseRandom(game.getAvailableActions());
				else
					return Action.announcePassz();
			case FOLDING:
				List<Card> cardsToSkart = cards.filter(new SkartableCardFilter(game.getGameType())).subList(0, cards.size() - 9);
				return Action.fold(cardsToSkart);
			case GAMEPLAY:
				Trick currentTrick = game.getTrick(game.getTrickCount() - 1);
				Action.play(chooseRandom(cards.getPlayableCards(currentTrick.getFirstCard())));
			case END:
			case INTERRUPTED:
				return Action.readyForNewGame();
		}
		throw new RuntimeException();
	}

	private <T> T chooseRandom(Collection<T> from)
	{
		int n = rnd.nextInt(from.size());
		Iterator<T> it = from.iterator();
		for (int i = 0; i < n; i++)
		{
			it.next();
		}
		return it.next();
	}
}
