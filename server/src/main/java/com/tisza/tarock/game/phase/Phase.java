package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import org.apache.log4j.*;

import java.util.*;

abstract class Phase implements ActionHandler
{
	private static final Logger log = Logger.getLogger(Phase.class);

	protected final Game game;

	public Phase(Game game)
	{
		this.game = game;
	}

	public abstract PhaseEnum asEnum();
	public abstract void onStart();

	public List<Action> getAvailableActions()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean announce(PlayerSeat player, AnnouncementContra announcementContra)
	{
		return wrongPhase("announce");
	}

	@Override
	public boolean announcePassz(PlayerSeat player)
	{
		return wrongPhase("announcePassz");
	}

	@Override
	public boolean bid(PlayerSeat player, int bid)
	{
		return wrongPhase("bid");
	}

	@Override
	public boolean call(PlayerSeat player, Card card)
	{
		return wrongPhase("call");
	}

	@Override
	public boolean fold(PlayerSeat player, List<Card> cards)
	{
		return wrongPhase("change");
	}

	@Override
	public boolean playCard(PlayerSeat player, Card card)
	{
		return wrongPhase("playCard");
	}

	@Override
	public boolean readyForNewGame(PlayerSeat player)
	{
		return wrongPhase("readyForNewGame");
	}

	@Override
	public boolean throwCards(PlayerSeat player)
	{
		return wrongPhase("throwCards");
	}

	private boolean wrongPhase(String action)
	{
		log.warn("Phase: " + asEnum() + " does not support action: " + action);
		return false;
	}
}
