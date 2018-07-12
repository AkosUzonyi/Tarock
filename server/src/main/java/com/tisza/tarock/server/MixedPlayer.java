package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class MixedPlayer implements Player
{
	private final Player player0, player1;
	private final PhaseEnum changePhase;

	private Player activePlayer;

	private PhaseEnum currentPhase;

	private BroadcastEventSender eventSender = new BroadcastEventSender();
	private ActionHandler actionHandler;
	private PlayerSeat seat;

	public MixedPlayer(Player player0, Player player1, PhaseEnum changePhase)
	{
		if (!player0.getName().equals(player1.getName()))
			throw new IllegalArgumentException("player names must match");

		this.player0 = player0;
		this.player1 = player1;
		this.changePhase = changePhase;
	}

	private void changeActivePlayer(Player player)
	{
		if (player == activePlayer)
			return;

		if (activePlayer != null)
		{
			activePlayer.onRemovedFromGame();
		}

		activePlayer = player;

		if (activePlayer != null)
		{
			activePlayer.onAddedToGame(actionHandler, seat);
			eventSender.setEventSenders(Arrays.asList(activePlayer.getEventSender(), phaseTrackerEventSender));
			actionHandler.requestHistory(seat);
		}
	}

	@Override
	public String getName()
	{
		return player0.getName();
	}

	@Override
	public EventSender getEventSender()
	{
		return eventSender;
	}

	@Override
	public void onAddedToGame(ActionHandler actionHandler, PlayerSeat seat)
	{
		this.actionHandler = actionHandler;
		this.seat = seat;

		changeActivePlayer(player0);
	}

	@Override
	public void onRemovedFromGame()
	{
		changeActivePlayer(null);
	}

	private EventSender phaseTrackerEventSender = new DummyEventSender()
	{
		@Override
		public void phaseChanged(PhaseEnum phase)
		{
			currentPhase = phase;
			if (currentPhase == changePhase)
			{
				changeActivePlayer(player1);
			}
		}
	};
}
