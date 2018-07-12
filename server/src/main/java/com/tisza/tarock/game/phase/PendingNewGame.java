package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private PlayerSeatMap<Boolean> ready = new PlayerSeatMap<>(false);

	public PendingNewGame(GameState game, boolean doubleRound)
	{
		super(game);
		this.doubleRound = doubleRound;
	}

	@Override
	public PhaseEnum asEnum()
	{
		return doubleRound ? PhaseEnum.INTERRUPTED : PhaseEnum.END;
	}

	@Override
	public void onStart()
	{
		if (!doubleRound)
		{
			game.calculateStatistics();
			game.sendStatistics();
			game.sendPlayerPoints();
		}

		game.broadcastEvent(Event.pendingNewGame());
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		ready.put(player, true);
		game.broadcastEvent(Event.readyForNewGame(player));

		if (allReady())
			game.finish();
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
