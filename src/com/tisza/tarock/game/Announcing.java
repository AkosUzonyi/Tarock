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
		
		if (!getAvailableAnnouncements().contains(announcement))
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
		
		if (!getAvailableContras().contains(contra))
			return false;
		
		Team team = playerPairs.getTeam(player);
		
		idTrack.identityRevealed(player);
		currentPlayerAnnounced = true;
		
		announcementStates.get(contra.getAnnouncement()).team(team).contra();
		return true;
	}
	
	private boolean requireContra()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		Team team = playerPairs.getTeam(currentPlayer);
		
		if (lastAnnouncer >= 0 && team != playerPairs.getTeam(lastAnnouncer) && !idTrack.isIdentityKnown(currentPlayer))
			return true;
		
		return false;
	}
	
	public List<Announcement> getAvailableAnnouncements()
	{
		if (isFinished())
			return null;
		
		List<Announcement> result = new ArrayList<Announcement>();
		
		if (requireContra()) return result;
		
		for (Announcement a : Announcements.getAll())
		{
			boolean announced = announcementStates.get(a).team(playerPairs.getTeam(currentPlayer)).isAnnounced();
			if (!announced && a.canAnnounce(announcementStates, playerCards.getPlayerCards(currentPlayer), currentPlayer, playerPairs));
			{
				if (a instanceof Banda) System.out.println(a.getClass().getSimpleName());
				result.add(a);
			}
		}
		return result;
	}
	
	public List<Contra> getAvailableContras()
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