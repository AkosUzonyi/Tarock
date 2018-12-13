package com.tisza.tarock.game;

import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import org.json.*;

import java.io.*;
import java.text.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.*;

public class GameSession implements GameFinishedListener
{
	private final File saveDir;
	private final GameType gameType;
	private final PlayerSeat.Map<Player> players = new PlayerSeat.Map<>();

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
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			players.get(seat).onAddedToGame(new GameSessionActionHandler(this), seat);
		}

		startNewGame();
	}

	public void stopSession()
	{
		currentGame.stop();
		currentGame = null;

		for (Player p : players)
		{
			p.onRemovedFromGame();
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
		currentGame.start();
	}

	@Override
	public int[] pointsEarned(int[] points)
	{
		for (int i = 0; i < 4; i++)
		{
			this.points[i] += points[i];
		}
		return this.points;
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
