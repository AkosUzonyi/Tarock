package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Announcing
{
	private AllPlayersCards playerCards;
	private final PlayerPairs playerPairs;
	private int currentPlayer;
	
	private boolean currentPlayerAnnounced = false;
	private int lastAnnouncer = -1;
	private IdentityTracker idTrack;
	
	private Map<Announcement, AnnouncementState> announcementStates = new HashMap<Announcement, AnnouncementState>();	
	
	public Announcing(AllPlayersCards playerCards, PlayerPairs playerPairs, Invitation invit)
	{
		this.playerCards = playerCards;
		this.playerPairs = playerPairs;
		currentPlayer = playerPairs.getCaller();
		idTrack = new IdentityTracker(playerPairs, invit);
		
		for (Announcement a : Announcements.getAll())
		{
			announcementStates.put(a, new AnnouncementState());
		}
		
		announce(playerPairs.getCaller(), Announcements.game);
	}
	
	public int getNextPlayer()
	{
		return currentPlayer;
	}
	
	public Map<Announcement, AnnouncementState> getAnnouncementStates()
	{
		return announcementStates;
	}

	public boolean announce(int player, Announcement announcement)
	{
		if (isFinished())
			return false;
		
		if (player != currentPlayer)
			return false;
		
		if (announcement == null)
		{
			if (currentPlayerAnnounced)
			{
				lastAnnouncer = currentPlayer;
			}
			currentPlayer++;
			currentPlayer %= 4;
			currentPlayerAnnounced = false;
			return true;
		}
		
		Team team = playerPairs.getTeam(player);
		
		if (lastAnnouncer >= 0 && team != playerPairs.getTeam(lastAnnouncer) && !idTrack.isIdentityKnown(player))
			return false;
		
		if (!isAnnouncementValid(announcement))
			return false;
		
		idTrack.identityRevealed(player);
		currentPlayerAnnounced = true;
		
		return announcementStates.get(announcement).team(team).announce();
	}
	
	public boolean contra(int player, Contra contra)
	{
		if (isFinished())
			return false;
		
		if (player != currentPlayer)
			return false;
		
		Team playerTeam = playerPairs.getTeam(player);
		Team announcerTeam = contra.isSelf() ? playerTeam : playerTeam.getOther();
		AnnouncementState.PerTeam as = announcementStates.get(contra.getAnnouncement()).team(announcerTeam);
		if (as.isAnnounced() && as.getNextTeamToContra() == playerTeam && as.getContraLevel() == contra.getLevel())
		{
			as.contra();
			idTrack.identityRevealed(player);
			currentPlayerAnnounced = true;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean canAnnounce()
	{
		if (isFinished())
			return false;
		
		Team team = playerPairs.getTeam(currentPlayer);
		
		if (lastAnnouncer >= 0 && team != playerPairs.getTeam(lastAnnouncer) && !idTrack.isIdentityKnown(currentPlayer))
			return false;
		
		return true;
	}
	
	public List<Contra> getPossibleContras()
	{
		if (isFinished())
			return null;
		
		List<Contra> result = new ArrayList<Contra>();
		for (Announcement a : announcementStates.keySet())
		{
			AnnouncementState as = announcementStates.get(a);
			for (Team t : Team.values())
			{
				AnnouncementState.PerTeam ast = as.team(t);
				if (ast.isAnnounced() && ast.getNextTeamToContra() == playerPairs.getTeam(currentPlayer))
				{
					result.add(new Contra(a, ast.getContraLevel()));
				}
			}
		}
		return result;
	}
	
	public boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private boolean isAnnouncementValid(Announcement announcement)
	{
		Team team = playerPairs.getTeam(currentPlayer);
		
		if (announcement == Announcements.hosszuDupla)
		{
			if (playerCards.getPlayerCards(currentPlayer).filter(new TarockFilter()).size() < 7)
			{
				return false;
			}
		}
		
		if (announcement instanceof Banda)
		{
			for (Banda banda : Announcements.bandak)
			{
				if (announcementStates.get(banda).team(team).isAnnounced())
				{
					return false;
				}
			}
		}
		
		
		
		return true;
	}
	
	private static class IdentityTracker
	{
		private final PlayerPairs playerPairs;
		private final Invitation invit;
		private boolean[] identityKnown = new boolean[4];
		
		public IdentityTracker(PlayerPairs pp, Invitation i)
		{
			playerPairs = pp;
			invit = i;
			identityKnown[playerPairs.getCaller()] = true;
			if (invit != Invitation.NONE)
			{
				Arrays.fill(identityKnown, true);
			}
		}
		
		public void identityRevealed(int player)
		{
			if (player == playerPairs.getCalled())
			{
				Arrays.fill(identityKnown, true);
			}
			identityKnown[player] = true;
		}
		
		public boolean isIdentityKnown(int player)
		{
			return identityKnown[player];
		}
	}
}