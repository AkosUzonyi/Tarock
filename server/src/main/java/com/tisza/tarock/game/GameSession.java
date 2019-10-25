package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.*;
import io.reactivex.*;
import io.reactivex.Observable;

import java.util.*;
import java.util.stream.*;

public class GameSession implements Game
{
	private final int id;
	private final Database database;
	private final GameType gameType;
	private final PlayerSeatMap<Player> players = new PlayerSeatMap<>();
	private final Set<Player> allPlayers = new HashSet<>();

	private final DoubleRoundTracker doubleRoundTracker;

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private Single<Integer> currentGameID;
	private int actionOrdinal = 0;

	private int[] points = new int[4];

	private GameSession(int id, GameType gameType, List<? extends Player> playerList, DoubleRoundType doubleRoundType, Database database)
	{
		if (playerList.size() != 4)
			throw new IllegalArgumentException("GameSession: playerList.size() != 4");

		this.id = id;
		this.database = database;
		this.gameType = gameType;

		for (int i = 0; i < 4; i++)
		{
			PlayerSeat playerSeat = PlayerSeat.fromInt(i);
			Player player = playerList.get(i);

			players.put(playerSeat, player);
			allPlayers.add(player);
			player.setGame(this, playerSeat);
		}

		doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);
	}

	public static Single<GameSession> create(GameType gameType, List<? extends Player> playerList, DoubleRoundType doubleRoundType, Database database)
	{
		return database.createGameSession(gameType, doubleRoundType).map(id -> new GameSession(id, gameType, playerList, doubleRoundType, database));
	}

	public int getID()
	{
		return id;
	}

	public void startSession()
	{
		startNewGame();
	}

	public void stopSession()
	{
		currentGame = null;
		dispatchEvent(new EventInstance(null, Event.deleteGame()));
		for (Player player : allPlayers)
			player.setGame(null, null);

		database.stopGameSession(id);
	}

	public void addKibic(Player player)
	{
		if (allPlayers.add(player))
			player.setGame(this, null);
	}

	public void removeKibic(Player player)
	{
		if (!players.containsValue(player) && allPlayers.remove(player))
			player.setGame(null, null);
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public List<String> getPlayerNames()
	{
		return players.values().stream().map(Player::getName).collect(Collectors.toList());
	}

	private void startNewGame()
	{
		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);

		if (database != null)
		{
			currentGameID = database.createGame(id, currentBeginnerPlayer).cache();
			currentGameID.subscribe(gid -> database.setDeck(gid, deck));
		}

		currentGame = new GameState(gameType, getPlayerNames(), currentBeginnerPlayer, deck, points, doubleRoundTracker.getCurrentMultiplier());
		currentGame.start();
		dispatchNewEvents();
	}

	@Override
	public void action(Action action)
	{
		boolean success = currentGame.processAction(action);
		if (success && database != null)
		{
			int ordinal = actionOrdinal++;
			currentGameID.subscribe(gameID -> database.addAction(gameID, action, ordinal));
		}

		dispatchNewEvents();

		if (currentGame.isFinished())
		{
			if (currentGame.isNormalFinish())
			{
				currentBeginnerPlayer = currentBeginnerPlayer.nextPlayer();
				doubleRoundTracker.gameFinished();
			}
			else
			{
				doubleRoundTracker.gameInterrupted();
			}

			if (database != null)
				for (int i = 0; i < 4; i++)
					database.setPlayerPoints(id, i, points[i]);

			startNewGame();
		}
	}

	private void dispatchNewEvents()
	{
		EventInstance event;
		while ((event = currentGame.popNextEvent()) != null)
			dispatchEvent(event);
	}

	private void dispatchEvent(EventInstance event)
	{
		if (event.getPlayerSeat() == null)
			for (Player player : allPlayers)
				event.getEvent().handle(player.getEventHandler());
		else
			event.getEvent().handle(players.get(event.getPlayerSeat()).getEventHandler());
	}

	@Override
	public void requestHistory(PlayerSeat seat, EventHandler eventHandler)
	{
		for (EventInstance event : currentGame.getAllEvents())
			if (event.getPlayerSeat() == null || event.getPlayerSeat() == seat)
				event.getEvent().handle(eventHandler);
	}
}
