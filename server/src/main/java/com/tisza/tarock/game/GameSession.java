package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.*;
import io.reactivex.Observable;
import io.reactivex.*;
import org.davidmoten.rx.jdbc.tuple.*;

import java.util.*;
import java.util.stream.*;

public class GameSession implements Game
{
	private final int id;
	private final GameType gameType;
	private final PlayerSeatMap<Player> players = new PlayerSeatMap<>();
	private final Set<Player> allPlayers = new HashSet<>();

	private final DoubleRoundTracker doubleRoundTracker;

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private TarockDatabase database;
	private Single<Integer> currentGameID;
	private int actionOrdinal = 0;

	private int[] points = new int[4];

	private GameSession(int id, GameType gameType, List<Player> playerList, DoubleRoundTracker doubleRoundTracker, TarockDatabase database)
	{
		if (playerList.size() != 4)
			throw new IllegalArgumentException("GameSession: playerList.size() != 4");

		this.id = id;
		this.database = database;
		this.gameType = gameType;
		this.doubleRoundTracker = doubleRoundTracker;

		for (int i = 0; i < 4; i++)
		{
			PlayerSeat playerSeat = PlayerSeat.fromInt(i);
			Player player = playerList.get(i);

			players.put(playerSeat, player);
			allPlayers.add(player);
			player.setGame(this, playerSeat);
		}
	}

	public static Single<GameSession> createNew(GameType gameType, List<User> users, DoubleRoundType doubleRoundType, TarockDatabase database)
	{
		if (users.size() != 4)
			throw new IllegalArgumentException("users.size() != 4: " + users.size());

		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);

		return
		Observable.fromIterable(users).flatMapSingle(User::createPlayer).toList().flatMap(players ->
		database.createGameSession(gameType, doubleRoundTracker).map(id ->
		{
			Collections.shuffle(players);

			for (PlayerSeat seat : PlayerSeat.getAll())
			{
				Player player = players.get(seat.asInt());
				database.addPlayer(id, seat, player.getUser());
			}

			return new GameSession(id, gameType, players, doubleRoundTracker, database);
		}));
	}

	public static Single<GameSession> load(int id, TarockDatabase database)
	{
		return
		database.getGameSession(id).flatMap(gameSessionTuple ->
		database.getUsersForGameSession(id).flatMapSingle(User::createPlayer).toList().flatMap(players ->
		database.getPlayerPoints(id).toList().flatMap(points ->
		database.getActions(gameSessionTuple._4()).toList().flatMap(actions ->
		database.getDeck(gameSessionTuple._4()).toList().flatMap(deck ->
		database.getGameBeginner(gameSessionTuple._4()).map(beginnerPlayer ->
		{
			GameType gameType = gameSessionTuple._1();
			DoubleRoundType doubleRoundType = gameSessionTuple._2();
			int doubleRoundData = gameSessionTuple._3();
			int currentGameID = gameSessionTuple._4();

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);
			doubleRoundTracker.setData(doubleRoundData);

			GameSession gameSession = new GameSession(id, gameType, players, doubleRoundTracker, database);

			gameSession.currentBeginnerPlayer = beginnerPlayer;
			gameSession.currentGameID = Single.just(currentGameID);
			gameSession.actionOrdinal = actions.size();

			for (int i = 0; i < 4; i++)
				gameSession.points[i] = points.get(i);

			gameSession.currentGame = new GameState(gameType, gameSession.getPlayerNames(), beginnerPlayer, deck, gameSession.points, doubleRoundTracker.getCurrentMultiplier());
			gameSession.currentGame.start();

			for (Tuple3<PlayerSeat, Action, Integer> action : actions)
				gameSession.currentGame.processAction(action._1(), action._2());

			gameSession.dispatchEvent(new EventInstance(null, Event.historyMode(true)));
			gameSession.dispatchNewEvents();
			gameSession.dispatchEvent(new EventInstance(null, Event.historyMode(false)));

			return gameSession;
		}))))));
	}

	public int getID()
	{
		return id;
	}

	public PlayerSeatMap<Player> getPlayers()
	{
		return players;
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

	public Player getPlayerByUser(User user)
	{
		for (Player player : players.values())
			if (player.getUser().equals(user))
				return player;

		return null;
	}

	public boolean isUserPlaying(User user)
	{
		return getPlayerByUser(user) != null;
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
			actionOrdinal = 0;
		}

		currentGame = new GameState(gameType, getPlayerNames(), currentBeginnerPlayer, deck, points, doubleRoundTracker.getCurrentMultiplier());
		currentGame.start();
		dispatchNewEvents();
	}

	@Override
	public void action(PlayerSeat player, Action action)
	{
		if (currentGame == null)
			return;

		boolean success = currentGame.processAction(player, action);
		if (success && database != null)
		{
			int ordinal = actionOrdinal++;
			currentGameID.subscribe(gameID -> database.addAction(gameID, player.asInt(), action, ordinal));
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
			{
				database.setDoubleRoundData(id, doubleRoundTracker.getData());

				for (PlayerSeat seat : PlayerSeat.getAll())
					database.setPlayerPoints(id, seat, points[seat.asInt()]);
			}

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
				player.handleEvent(event.getEvent());
		else
			players.get(event.getPlayerSeat()).handleEvent(event.getEvent());
	}

	@Override
	public void requestHistory(Player player)
	{
		if (currentGame == null)
			return;

		player.handleEvent(Event.historyMode(true));
		for (EventInstance event : currentGame.getAllEvents())
			if (event.getPlayerSeat() == null || event.getPlayerSeat() == player.getSeat())
				player.handleEvent(event.getEvent());
		player.handleEvent(Event.historyMode(false));
	}
}
