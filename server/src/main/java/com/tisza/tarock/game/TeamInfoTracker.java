package com.tisza.tarock.game;

import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class TeamInfoTracker implements EventSender
{
	private final GameState game;
	private Set<TeamInfoKnowledge> teamInfoKnowledges = new HashSet<>();
	private Card calledCard;

	public TeamInfoTracker(GameState game)
	{
		this.game = game;
	}

	private void addNewTeamInfo(PlayerSeat player, PlayerSeat otherPlayer)
	{
		if (teamInfoKnowledges.add(new TeamInfoKnowledge(player, otherPlayer)))
			game.getPlayerEventSender(player).playerTeamInfo(otherPlayer, game.getPlayerPairs().getTeam(otherPlayer));
	}

	private void revealAllTeamInfoOf(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			addNewTeamInfo(p, player);
		}
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
			for (PlayerSeat otherPlayer : PlayerSeat.getAll())
			{
				addNewTeamInfo(player, otherPlayer);
			}
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

	public void sendStatusToPlayer(PlayerSeat player)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			if (hasTeamInfo(player, p))
				game.getPlayerEventSender(player).playerTeamInfo(p, game.getPlayerPairs().getTeam(p));
		}
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		calledCard = card;

		for (PlayerSeat p : PlayerSeat.getAll())
		{
			addNewTeamInfo(p, p);
		}

		if (!game.getPlayerPairs().isSolo() || game.isSoloIntentional())
			revealAllTeamInfoFor(game.getPlayerPairs().getCalled());

		if (game.getInvitAccepted() != Invitation.NONE)
		{
			revealAllTeamInfo();
		}
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
