package com.tisza.tarock.game;

public class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private boolean[] ready = new boolean[4];

	public PendingNewGame(GameState gameState, boolean doubleRound)
	{
		super(gameState);
		this.doubleRound = doubleRound;
	}

	public PhaseEnum asEnum()
	{
		return doubleRound ? PhaseEnum.INTERRUPTED : PhaseEnum.END;
	}

	public void onStart()
	{
		gameState.getEventQueue().broadcast().pendingNewGame();
	}

	public boolean readyForNewGame(int player)
	{
		ready[player] = true;
		if (allReady())
		{
			gameState.startNewGame(doubleRound);
		}
		return true;
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
