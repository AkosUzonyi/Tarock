package com.tisza.tarock.game;

import com.tisza.tarock.game.doubleround.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.stream.*;

public class GameSession implements GameFinishedListener
{
	private final GameType gameType;
	private final PlayerSeat.Map<Player> players = new PlayerSeat.Map<>();

	private final DoubleRoundTracker doubleRoundTracker;

	private PlayerSeat currentBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private int[] points = new int[4];

	public GameSession(GameType gameType, List<? extends Player> playerList, DoubleRoundType doubleRoundType)
	{
		if (players.size() != 4)
			throw new IllegalArgumentException();

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

	@Override
	public void gameFinished()
	{
		currentBeginnerPlayer = currentBeginnerPlayer.nextPlayer();
		doubleRoundTracker.gameFinished();
		startNewGame();
	}

	@Override
	public void gameInterrupted()
	{
		doubleRoundTracker.gameInterrupted();
		startNewGame();
	}
}
