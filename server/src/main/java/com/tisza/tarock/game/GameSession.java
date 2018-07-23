package com.tisza.tarock.game;

import com.tisza.tarock.message.*;

import java.util.*;
import java.util.stream.*;

public class GameSession implements GameFinishedListener
{
	private final GameType gameType;
	private final PlayerSeat.Map<Player> players = new PlayerSeat.Map<>();

	private PlayerSeat nextBeginnerPlayer = PlayerSeat.SEAT0;
	private GameState currentGame;

	private int[] points = new int[4];

	public GameSession(GameType gameType, List<? extends Player> playerList)
	{
		if (players.size() != 4)
			throw new IllegalArgumentException();

		this.gameType = gameType;

		for (int i = 0; i < 4; i++)
		{
			players.put(PlayerSeat.fromInt(i), playerList.get(i));
		}
	}

	public void startSession()
	{
		for (PlayerSeat seat : PlayerSeat.getAll())
		{
			players.get(seat).onAddedToGame(new GameSessionActionHandler(this), seat);
		}

		startNewGame(false);
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

	void startNewGame(boolean doubleRound)
	{
		currentGame = new GameState(gameType, players, nextBeginnerPlayer, this);

		if (!doubleRound)
			nextBeginnerPlayer = nextBeginnerPlayer.nextPlayer();

		currentGame.start();
	}

	@Override
	public void gameFinished(int[] points)
	{
		for (int i = 0; i < 4; i++)
		{
			this.points[i] += points[i];
		}
		startNewGame(false);
	}

	@Override
	public void gameInterrupted()
	{
		startNewGame(true);
	}
}
