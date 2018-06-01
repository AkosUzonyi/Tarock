package com.tisza.tarock.game;

class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private PlayerSeat.Map<Boolean> ready = new PlayerSeat.Map<>(false);

	public PendingNewGame(GameSession gameSession, boolean doubleRound)
	{
		super(gameSession);
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
		gameSession.getBroadcastEventSender().pendingNewGame();
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		if (!ready.get(player))
			gameSession.getBroadcastEventSender().pendingNewGame();
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		ready.put(player, true);
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
