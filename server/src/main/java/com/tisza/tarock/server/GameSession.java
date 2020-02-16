package com.tisza.tarock.server;

import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.player.*;
import io.reactivex.*;
import org.davidmoten.rx.jdbc.tuple.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class GameSession
{
	private final int id;
	private final GameType gameType;
	private final DoubleRoundTracker doubleRoundTracker;
	private final TarockDatabase database;

	private State state = State.LOBBY;
	private List<Player> players = new ArrayList<>();
	private Set<Player> watchingPlayers = new HashSet<>();

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private Game currentGame;

	private Single<Integer> currentGameID;
	private int actionOrdinal = 0;

	private long lastModified;
	private boolean historyView = false;

	private int[] points = new int[4];

	private GameSession(int id, GameType gameType, DoubleRoundTracker doubleRoundTracker, TarockDatabase database)
	{
		this.id = id;
		this.database = database;
		this.gameType = gameType;
		this.doubleRoundTracker = doubleRoundTracker;
		lastModified = System.currentTimeMillis();
	}

	public static Single<GameSession> createNew(GameType gameType, DoubleRoundType doubleRoundType, TarockDatabase database)
	{
		DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);

		return database.createGameSession(gameType, doubleRoundTracker).map(id -> new GameSession(id, gameType, doubleRoundTracker, database));
	}

	public static Single<GameSession> load(int id, TarockDatabase database)
	{
		return
		database.getGameSession(id).flatMap(gameSessionTuple ->
		database.getUsersForGameSession(id).flatMapSingle(User::createPlayer).toList().flatMap(players ->
		database.getPlayerPoints(id).toList().flatMap(points ->
		database.getActions(gameSessionTuple._4()).toList().flatMap(actions ->
		database.getDeck(gameSessionTuple._4()).toList().flatMap(deck ->
		database.getGame(gameSessionTuple._4()).map(gameTuple ->
		{
			GameType gameType = gameSessionTuple._1();
			DoubleRoundType doubleRoundType = gameSessionTuple._2();
			int doubleRoundData = gameSessionTuple._3();
			int currentGameID = gameSessionTuple._4();
			PlayerSeat beginnerPlayer = gameTuple._2();
			long lastGameCreateTime = gameTuple._3();

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);
			doubleRoundTracker.setData(doubleRoundData);

			GameSession gameSession = new GameSession(id, gameType, doubleRoundTracker, database);

			gameSession.state = State.GAME;
			gameSession.currentBeginnerPlayer = beginnerPlayer;
			gameSession.currentGameID = Single.just(currentGameID);
			gameSession.actionOrdinal = actions.size();
			gameSession.lastModified = lastGameCreateTime;

			for (int i = 0; i < 4; i++)
			{
				PlayerSeat seat = PlayerSeat.fromInt(i);
				Player player = players.get(i);
				gameSession.players.add(player);
				gameSession.watchingPlayers.add(player);
				player.setGame(gameSession, seat);

				gameSession.points[i] = points.get(i);
			}

			gameSession.currentGame = new Game(gameType, beginnerPlayer, deck, gameSession.points, doubleRoundTracker.getCurrentMultiplier());
			gameSession.currentGame.start();

			for (Tuple3<PlayerSeat, Action, Long> actionTuple : actions)
			{
				PlayerSeat player = actionTuple._1();
				Action action = actionTuple._2();
				long time = actionTuple._3();

				gameSession.currentGame.processAction(player, action);

				if (time > gameSession.lastModified)
					gameSession.lastModified = time;
			}

			gameSession.dispatchEvent(EventInstance.broadcast(Event.historyMode(true)));
			gameSession.dispatchNewEvents();
			gameSession.dispatchEvent(EventInstance.broadcast(Event.historyMode(false)));

			return gameSession;
		}))))));
	}

	public static Single<GameSession> createHistoryView(int gameID, TarockDatabase database)
	{
		return
		database.getGame(gameID).flatMap(gameTuple ->
		database.getGameSession(gameTuple._1()).flatMap(gameSessionTuple ->
		database.getUsersForGameSession(gameTuple._1()).flatMapSingle(User::createPlayer).toList().flatMap(players ->
		database.getActions(gameID).toList().flatMap(actions ->
		database.getDeck(gameID).toList().map(deck ->
		{
			int id = gameTuple._1();
			PlayerSeat beginnerPlayer = gameTuple._2();
			GameType gameType = gameSessionTuple._1();
			DoubleRoundType doubleRoundType = gameSessionTuple._2();
			long gameCreateTime = gameTuple._3();

			DoubleRoundTracker doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);

			GameSession gameSession = new GameSession(-1, gameType, doubleRoundTracker, database);

			gameSession.state = State.GAME;
			gameSession.currentBeginnerPlayer = beginnerPlayer;
			gameSession.historyView = true;

			for (int i = 0; i < 4; i++)
			{
				PlayerSeat seat = PlayerSeat.fromInt(i);
				Player player = players.get(i);
				gameSession.players.add(player);
				gameSession.watchingPlayers.add(player);
				player.setGame(gameSession, seat);
			}

			gameSession.currentGame = new Game(gameType, beginnerPlayer, deck, gameSession.points, doubleRoundTracker.getCurrentMultiplier());
			gameSession.currentGame.start();

			for (Tuple3<PlayerSeat, Action, Long> actionTuple : actions)
			{
				PlayerSeat player = actionTuple._1();
				Action action = actionTuple._2();
				long time = actionTuple._3();

				Main.GAME_EXECUTOR_SERVICE.schedule(() ->
				{
					gameSession.currentGame.processAction(player, action);
					gameSession.dispatchNewEvents();
				},
				time - gameCreateTime, TimeUnit.MILLISECONDS);
			}

			gameSession.dispatchNewEvents();

			return gameSession;
		})))));
	}

	private void checkAlive()
	{
		if (state == State.ENDED)
			throw new IllegalStateException("GameSession is ended");
	}

	private void checkIsLobby()
	{
		if (state != State.LOBBY)
			throw new IllegalStateException("GameSession is not a lobby");
	}

	private void checkGameStarted()
	{
		if (state != State.GAME)
			throw new IllegalStateException("GameSession is not in game state");
	}

	public int getID()
	{
		return id;
	}

	public List<Player> getPlayers()
	{
		checkAlive();
		return players;
	}

	public int getFreeLobbyPlaces()
	{
		checkIsLobby();
		return 4 - players.size();
	}

	public boolean isLobbyFull()
	{
		checkIsLobby();
		return getFreeLobbyPlaces() == 0;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public void start()
	{
		checkIsLobby();

		if (!isLobbyFull())
			throw new IllegalStateException("GameSession needs more players to start");

		state = State.GAME;

		Collections.shuffle(players);
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			Player player = players.get(seat.asInt());
			player.setGame(this, seat);
			database.addPlayer(id, seat, player.getUser());
		}

		startNewGame();
	}

	public State getState()
	{
		return state;
	}

	public void endSession()
	{
		currentGame = null;

		switch (state)
		{
			case LOBBY:
				database.deleteGameSession(id);
				break;
			case GAME:
				if (!historyView)
					database.endGameSession(id);
				break;
		}

		state = State.ENDED;
	}

	public boolean addPlayer(Player player)
	{
		checkIsLobby();

		if (isLobbyFull())
			return false;

		boolean userAlreadyInLobby = players.stream().anyMatch(p -> p.getUser().equals(player.getUser()));
		if (userAlreadyInLobby)
			return false;

		PlayerSeat seat = PlayerSeat.fromInt(players.size());
		players.add(player);
		watchingPlayers.add(player);
		player.setGame(this, seat);

		return true;
	}

	public boolean removePlayer(Player player)
	{
		checkIsLobby();

		int pos = players.indexOf(player);
		if (pos < 0)
			return false;

		PlayerSeat seat = PlayerSeat.fromInt(pos);
		players.remove(player);
		watchingPlayers.remove(player);
		player.setGame(null, null);

		if (!hasAnyRealPlayer())
			endSession();

		return true;
	}

	private boolean hasAnyRealPlayer()
	{
		for (Player player : players)
			if (!player.getUser().isBot())
				return true;

		return false;
	}

	public Player getPlayerByUser(User user)
	{
		checkAlive();

		for (Player player : players)
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
		checkGameStarted();

		if (watchingPlayers.add(player))
			player.setGame(this, null);
	}

	public void removeKibic(Player player)
	{
		checkGameStarted();

		if (!players.contains(player) && watchingPlayers.remove(player))
			player.setGame(null, null);
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public List<String> getPlayerNames()
	{
		checkAlive();
		return players.stream().map(Player::getName).collect(Collectors.toList());
	}

	private void startNewGame()
	{
		checkGameStarted();

		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);

		if (database != null)
		{
			currentGameID = database.createGame(id, currentBeginnerPlayer).cache();
			currentGameID.doOnSuccess(gid -> database.setDeck(gid, deck)).subscribe();
			actionOrdinal = 0;
		}

		currentGame = new Game(gameType, currentBeginnerPlayer, deck, points, doubleRoundTracker.getCurrentMultiplier());
		currentGame.start();
		dispatchNewEvents();
	}

	public void action(PlayerSeat player, Action action)
	{
		if (historyView)
			return;

		if (currentGame == null)
		{
			if (action.getChatString() != null)
				dispatchEvent(EventInstance.broadcast(Event.chat(player, action.getChatString())));

			return;
		}

		boolean success = currentGame.processAction(player, action);
		if (success && database != null)
		{
			int ordinal = actionOrdinal++;
			currentGameID.doOnSuccess(gameID -> database.addAction(gameID, player.asInt(), action, ordinal)).subscribe();
		}

		lastModified = System.currentTimeMillis();
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
		checkGameStarted();

		EventInstance event;
		while ((event = currentGame.popNextEvent()) != null)
			dispatchEvent(event);
	}

	private void dispatchEvent(EventInstance event)
	{
		if (event.getPlayerSeat() == null)
			for (Player player : watchingPlayers)
				player.handleEvent(event.getEvent());
		else
			players.get(event.getPlayerSeat().asInt()).handleEvent(event.getEvent());
	}

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

	public enum State
	{
		LOBBY, GAME, ENDED
	}
}
