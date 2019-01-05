package com.tisza.tarock.game;

import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import org.json.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

public class GameSession implements GameFinishedListener
{
	private final File saveDir;
	private final GameType gameType;
	private final PlayerSeatMap<Player> players = new PlayerSeatMap<>();
	private final Set<Player> kibices = new HashSet<>();

	private final DoubleRoundTracker doubleRoundTracker;

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private int[] points = new int[4];

	public GameSession(GameType gameType, List<? extends Player> playerList, DoubleRoundType doubleRoundType, File saveDir)
	{
		if (players.size() != 4)
			throw new IllegalArgumentException();

		this.saveDir = saveDir;
		this.gameType = gameType;

		for (int i = 0; i < 4; i++)
		{
			players.put(PlayerSeat.fromInt(i), playerList.get(i));
		}

		doubleRoundTracker = DoubleRoundTracker.createFromType(doubleRoundType);
	}

	public void startSession()
	{
		startNewGame();
	}

	public void stopSession()
	{
		currentGame.stop();
		currentGame = null;
		for (Player player : players)
			player.setGame(null, null);
		for (Player kibic : kibices)
			kibic.setGame(null, null);
	}

	public void addKibic(Player player)
	{
		if (kibices.add(player))
		{
			player.setGame(currentGame, null);
			currentGame.addKibic(player);
		}
	}

	public void removeKibic(Player player)
	{
		if (kibices.remove(player))
		{
			player.setGame(null, null);
			currentGame.removeKibic(player);
		}
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
		currentGame = new GameState(gameType, players, currentBeginnerPlayer, this, doubleRoundTracker.getCurrentMultiplier());
		for (PlayerSeat seat : PlayerSeat.getAll())
			players.get(seat).setGame(currentGame, seat);
		for (Player kibic : kibices)
		{
			kibic.setGame(currentGame, null);
			currentGame.addKibic(kibic);
		}
		currentGame.start();
	}

	@Override
	public int[] getPlayerPoints()
	{
		return points;
	}

	@Override
	public void pointsEarned(int[] points)
	{
		for (int i = 0; i < 4; i++)
		{
			this.points[i] += points[i];
		}
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

	@Override
	public void gameFinished()
	{
		currentBeginnerPlayer = currentBeginnerPlayer.nextPlayer();
		doubleRoundTracker.gameFinished();
		saveGame();
		startNewGame();
	}

	@Override
	public void gameInterrupted()
	{
		doubleRoundTracker.gameInterrupted();
		startNewGame();
	}
}
