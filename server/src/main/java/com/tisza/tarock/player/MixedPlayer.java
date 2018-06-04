package com.tisza.tarock.player;

import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.concurrent.*;

public class MixedPlayer implements Player
{
	private final Player player0, player1;
	private final PhaseEnum changePhase;

	private Player activePlayer;

	private PhaseEnum currentPhase;

	private BroadcastEventSender eventSender = new BroadcastEventSender();
	private BlockingQueue<Action> actionQueue;
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
			activePlayer.onDisconnectedFromGame();
		}

		activePlayer = player;

		if (activePlayer != null)
		{
			activePlayer.onJoinedToGame(actionQueue, seat);
			eventSender.setEventSenders(Arrays.asList(activePlayer.getEventSender(), phaseTrackerEventSender));
			actionQueue.add(handler -> handler.requestHistory(seat));
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
	public void onJoinedToGame(BlockingQueue<Action> actionQueue, PlayerSeat seat)
	{
		this.actionQueue = actionQueue;
		this.seat = seat;

		changeActivePlayer(player0);
	}

	@Override
	public void onDisconnectedFromGame()
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
