package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import org.json.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

public class GameSession implements Game
{
	private final File saveDir;
	private final GameType gameType;
	private final PlayerSeatMap<Player> players = new PlayerSeatMap<>();
	private final Set<Player> allPlayers = new HashSet<>();

	private final DoubleRoundTracker doubleRoundTracker;

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private int[] points = new int[4];

	public GameSession(GameType gameType, List<? extends Player> playerList, DoubleRoundType doubleRoundType, File saveDir)
	{
		if (playerList.size() != 4)
			throw new IllegalArgumentException("GameSession: playerList.size() != 4");

		this.saveDir = saveDir;
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

	public void startSession()
	{
		startNewGame();
	}

	public void stopSession()
	{
		currentGame = null;
		dispatchEvents(Arrays.asList(new EventInstance(null, Event.deleteGame())));
		for (Player player : allPlayers)
			player.setGame(null, null);
	}

	public void addKibic(Player player)
	{
		if (allPlayers.add(player))
			player.setGame(this, null);
	}

	public void removeKibic(Player player)
	{
		if (allPlayers.remove(player))
			player.setGame(null, null);
	}

	public GameType getGameType()
	{
		return gameType;
	}

	public GameState getCurrentGame()
	{
		return currentGame;
	}

	public List<String> getPlayerNames()
	{
		return players.values().stream().map(Player::getName).collect(Collectors.toList());
	}

	private void startNewGame()
	{
		List<Card> deck = new ArrayList<>(Card.getAll());
		Collections.shuffle(deck);
		currentGame = new GameState(gameType, getPlayerNames(), currentBeginnerPlayer, deck, points, doubleRoundTracker.getCurrentMultiplier());
		List<EventInstance> events = currentGame.start();
		dispatchEvents(events);
	}

	@Override
	public void action(Action action)
	{
		List<EventInstance> eventInstances = currentGame.processAction(action);
		dispatchEvents(eventInstances);

		if (currentGame.isFinished())
		{
			if (currentGame.isNormalFinish())
			{
				currentBeginnerPlayer = currentBeginnerPlayer.nextPlayer();
				doubleRoundTracker.gameFinished();
				saveGame();
			}
			else
			{
				doubleRoundTracker.gameInterrupted();
			}
			startNewGame();
		}
	}

	private void dispatchEvents(List<EventInstance> events)
	{
		for (EventInstance event : events)
		{
			if (event.getPlayerSeat() == null)
				for (Player player : allPlayers)
					event.getEvent().handle(player.getEventHandler());
			else
				event.getEvent().handle(players.get(event.getPlayerSeat()).getEventHandler());
		}
	}

	@Override
	public void requestHistory(PlayerSeat seat, EventHandler eventHandler)
	{
		for (EventInstance event : currentGame.getEvents())
			if (event.getPlayerSeat() == null || event.getPlayerSeat() == seat)
				event.getEvent().handle(eventHandler);
	}

	private void saveGame()
	{
		if (saveDir == null)
			return;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String date = dateFormat.format(Calendar.getInstance().getTime());
		File saveFile;
		for (int i = 0;; i++)
		{
			saveFile = new File(saveDir, date + (i == 0 ? "" : "_" + i));
			if (!saveFile.exists())
				break;
		}

		JSONObject json = new JSONObject();
		json.put("players", getPlayerNames());
		json.put("game", currentGame.getHistory().toJSON());

		try (FileWriter writer = new FileWriter(saveFile))
		{
			json.write(writer);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
