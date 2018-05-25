package com.tisza.tarock.game;

public class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private boolean[] ready = new boolean[4];

	public PendingNewGame(GameSession gameSession, boolean doubleRound)
	{
		super(gameSession);
		this.doubleRound = doubleRound;
	}

	public PhaseEnum asEnum()
	{
		return doubleRound ? PhaseEnum.INTERRUPTED : PhaseEnum.END;
	}

	public void onStart()
	{
		gameSession.getBroadcastEventSender().pendingNewGame();
	}

	public void readyForNewGame(int player)
	{
		ready[player] = true;
		if (allReady())
		{
			gameSession.startNewGame(doubleRound);
		}
		gameSession.getBroadcastEventSender().readyForNewGame(player);
	}

	private boolean allReady()
	{
		for (boolean r : ready)
		{
			if (!r) return false;
		}
		return true;
	}
}
