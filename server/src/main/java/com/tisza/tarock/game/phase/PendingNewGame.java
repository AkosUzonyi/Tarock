package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private PlayerSeatMap<Boolean> ready = new PlayerSeatMap<>(false);

	public PendingNewGame(Game game, boolean doubleRound)
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
			game.sendStatistics();

		game.broadcastEvent(Event.pendingNewGame());
	}

	@Override
	public boolean readyForNewGame(PlayerSeat player)
	{
		@SuppressWarnings("ConstantConditions")
		boolean wasReady = ready.put(player, true);
		if (wasReady)
			return false;

		game.broadcastEvent(Event.readyForNewGame(player));

		if (allReady())
			game.finish();

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
