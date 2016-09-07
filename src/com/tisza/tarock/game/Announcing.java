package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Announcing
{
	private AllPlayersCards playerCards;
	private final PlayerPairs playerPairs;
	private int currentPlayer;
	
	private boolean currentPlayerAnnounced = false;
	private int lastAnnouncer = -1;
	private IdentityTracker idTrack;
	
	//-1 if not announced
	private Map<Team, Map<Announcement, Integer>> announcementContraLevels = new HashMap<Team, Map<Announcement, Integer>>();	
	
	public Announcing(AllPlayersCards playerCards, PlayerPairs playerPairs, Invitation invit)
	{
		this.playerCards = playerCards;
		this.playerPairs = playerPairs;
		currentPlayer = playerPairs.getCaller();
		idTrack = new IdentityTracker(playerPairs, invit);
		
		for (Team team : Team.values())
		{
			Map<Announcement, Integer> map = new HashMap<Announcement, Integer>();
			for (Announcement a : Announcements.getAll())
			{
				map.put(a, -1);
			}
			announcementContraLevels.put(team, map);
		}
		
		announce(playerPairs.getCaller(), new AnnouncementContra(Announcements.game, 0));
	}

	public boolean announce(int player, AnnouncementContra ac)
	{
		if (isFinished())
			return false;
		
		if (player != currentPlayer)
			return false;
		
		if (ac == null)
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
		
		if (!getAvailableAnnouncements().contains(ac))
			return false;
		
		idTrack.identityRevealed(player);
		currentPlayerAnnounced = true;
		
		announcementContraLevels.get(ac.getNextTeamToContra(team)).put(ac.getAnnouncement(), ac.getContraLevel());
		System.out.println(announcementContraLevels.get(team).get(Announcements.trull));
		return true;
	}
	
	private boolean requireContra()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (lastAnnouncer < 0)
			return false;
		
		Team currentPlayerTeam = playerPairs.getTeam(currentPlayer);
		Team lastAnnouncerTeam = playerPairs.getTeam(lastAnnouncer);
		
		if (currentPlayerTeam != lastAnnouncerTeam && !idTrack.isIdentityKnown(currentPlayer))
			return true;
		
		return false;
	}
	
	public List<AnnouncementContra> getAvailableAnnouncements()
	{
		if (isFinished())
			return null;
		
		List<AnnouncementContra> result = new ArrayList<AnnouncementContra>();
		
		Team currentPlayerTeam = playerPairs.getTeam(currentPlayer);
		boolean canNormalAnnounce = !requireContra();
		
		for (Team t : Team.values())
		{
			for (Announcement a : Announcements.getAll())
			{
				if (isAnnounced(t, a))
				{
					AnnouncementContra ac = new AnnouncementContra(a, getContraLevel(t, a) + 1);
					if (ac.getNextTeamToContra(t) == currentPlayerTeam)
					{
						result.add(ac);
					}
				}
				else
				{
					if (canNormalAnnounce && t == currentPlayerTeam && a.canBeAnnounced(this))
					{
						result.add(new AnnouncementContra(a, 0));
					}
				}
			}
		}
		
		return result;
	}
	
	public int getNextPlayer()
	{
		return currentPlayer;
	}
	
	public boolean isAnnounced(Team team, Announcement a)
	{
		return announcementContraLevels.get(team).get(a) >= 0;
	}
	
	public int getContraLevel(Team team, Announcement a)
	{
		int contraLevel = announcementContraLevels.get(team).get(a);
		if (contraLevel < 0)
			throw new IllegalStateException();
		return contraLevel;
	}
	
	public AllPlayersCards getCards()
	{
		return playerCards;
	}
	
	public PlayerPairs getPlayerPairs()
	{
		return playerPairs;
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