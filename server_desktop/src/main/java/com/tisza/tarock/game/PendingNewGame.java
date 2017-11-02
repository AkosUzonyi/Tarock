package com.tisza.tarock.game;

import com.tisza.tarock.message.event.*;

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
		gameState.broadcastEvent(new EventPendingNewGame());
	}

	public void readyForNewGame(int player)
	{
		ready[player] = true;
		if (allReady())
		{
			gameState.startNewGame(doubleRound);
		}
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
