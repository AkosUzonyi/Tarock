package com.tisza.tarock.game;

import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class TeamInfoTracker implements EventHandler
{
	private final Game game;

	private Set<TeamInfoKnowledge> teamInfoKnowledges = new HashSet<>();

	private boolean inviterSkartedTarock = false;
	private Card calledCard;

	public TeamInfoTracker(Game game)
	{
		this.game = game;
	}

	private void addNewTeamInfo(PlayerSeat player, PlayerSeat otherPlayer)
	{
		if (teamInfoKnowledges.add(new TeamInfoKnowledge(player, otherPlayer)))
			game.sendEvent(player, Event.playerTeamInfo(otherPlayer, game.getPlayerPairs().getTeam(otherPlayer)));
	}

	private void revealAllTeamInfoOf(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			addNewTeamInfo(p, player);
		}
		game.broadcastEvent(Event.playerTeamInfo(player, game.getPlayerPairs().getTeam(player)));
	}

	private void revealAllTeamInfoFor(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			addNewTeamInfo(player, p);
		}
	}

	private void revealAllTeamInfo()
	{
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			revealAllTeamInfoOf(player);
		}
	}

	public boolean isTeamInfoGlobalOf(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			if (!hasTeamInfo(p, player))
				return false;
		}
		return true;
	}

	public boolean hasTeamInfo(PlayerSeat player, PlayerSeat otherPlayer)
	{
		return teamInfoKnowledges.contains(new TeamInfoKnowledge(player, otherPlayer));
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		calledCard = null;
		inviterSkartedTarock = false;
	}

	@Override
	public void foldTarock(PlayerSeatMap<Integer> counts)
	{
		if (game.getInvitSent() != null && counts.get(game.getInvitingPlayer()) > 0)
			inviterSkartedTarock = true;
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		calledCard = card;

		for (PlayerSeat p : PlayerSeat.getAll())
			addNewTeamInfo(p, p);

		if (!game.getPlayerPairs().isSolo() || game.isSoloIntentional())
			revealAllTeamInfoFor(game.getPlayerPairs().getCalled());

		if (game.getInvitAccepted() != null && !inviterSkartedTarock)
			revealAllTeamInfo();

		if (game.getSkart(game.getBidWinnerPlayer()).contains(card))
			revealAllTeamInfo();
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcement)
	{
		if (announcement.getAnnouncement() == Announcements.hkp)
		{
			revealAllTeamInfo();
		}
		else if (announcement.getAnnouncement().requireIdentification())
		{
			if (player == game.getPlayerPairs().getCalled() && !game.getPlayerPairs().isSolo())
			{
				revealAllTeamInfo();
			}
			else
			{
				revealAllTeamInfoOf(player);
			}
		}
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		if (card == calledCard)
		{
			revealAllTeamInfo();
		}
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		if (phase == PhaseEnum.END)
		{
			revealAllTeamInfo();
		}
	}

	private static class TeamInfoKnowledge
	{
		private final PlayerSeat player, otherPlayer;

		public TeamInfoKnowledge(PlayerSeat player, PlayerSeat otherPlayer)
		{
			if (player == null || otherPlayer == null)
				throw new NullPointerException();

			this.player = player;
			this.otherPlayer = otherPlayer;
		}

		@Override
		public int hashCode()
		{
			return player.asInt() << 16 | otherPlayer.asInt();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof TeamInfoKnowledge))
				return false;

			TeamInfoKnowledge other = (TeamInfoKnowledge)obj;
			return player == other.player && otherPlayer == other.otherPlayer;
		}
	}
}
