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

	private boolean inviterSkartedTarock = false;
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
		game.getBroadcastEventSender().playerTeamInfo(player, game.getPlayerPairs().getTeam(player));
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

	public void sendStatusToPlayer(PlayerSeat player, EventSender eventSender)
	{
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			boolean sendInfo = player == null ? isTeamInfoGlobalOf(p) : hasTeamInfo(player, p);
			if (sendInfo)
				eventSender.playerTeamInfo(p, game.getPlayerPairs().getTeam(p));
		}
	}

	@Override
	public void startGame(PlayerSeat seat, List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
	{
		calledCard = null;
		inviterSkartedTarock = false;
	}

	@Override
	public void skartTarock(PlayerSeatMap<Integer> counts)
	{
		if (game.getInvitSent() != Invitation.NONE && counts.get(game.getInvitingPlayer()) > 0)
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

		if (game.getInvitAccepted() != Invitation.NONE && !inviterSkartedTarock)
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
