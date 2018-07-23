package com.tisza.tarock.game;

class PendingNewGame extends Phase
{
	private boolean doubleRound;
	private PlayerSeat.Map<Boolean> ready = new PlayerSeat.Map<>(false);

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
		}

		game.getBroadcastEventSender().pendingNewGame();
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		if (!doubleRound)
			game.sendStatistics();

		if (!ready.get(player))
			game.getBroadcastEventSender().pendingNewGame();
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		ready.put(player, true);
		game.getBroadcastEventSender().readyForNewGame(player);

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
