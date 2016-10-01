package com.tisza.tarock.game;

import java.util.*;

import com.sun.xml.internal.ws.api.pipe.*;
import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Announcing
{
	private final AllPlayersCards playerCards;
	private final PlayerPairs playerPairs;
	private final int playerToAnnounceSolo;	

	private int currentPlayer;
	private boolean currentPlayerAnnounced = false;
	private int lastAnnouncer = -1;
	private IdentityTracker idTrack;
	
	private Map<Team, Map<Announcement, Integer>> announcementContraLevels = new HashMap<Team, Map<Announcement, Integer>>();
	
	public Announcing(AllPlayersCards playerCards, PlayerPairs playerPairs, Invitation invitAccepted, int playerToAnnounceSolo)
	{
		this.playerCards = playerCards;
		this.playerPairs = playerPairs;
		this.playerToAnnounceSolo = playerToAnnounceSolo;
		
		currentPlayer = playerPairs.getCaller();
		idTrack = new IdentityTracker(playerPairs, invitAccepted);
		
		for (Team team : Team.values())
		{
			Map<Announcement, Integer> map = new HashMap<Announcement, Integer>();
			announcementContraLevels.put(team, map);
		}
		
		announce(playerPairs.getCaller(), Announcements.jatek);
	}

	public boolean announce(int player, Announcement a)
	{
		return announce(player, new AnnouncementContra(a, 0));
	}

	public boolean announce(int player, AnnouncementContra ac)
	{
		if (isFinished())
			return false;
		
		if (player != currentPlayer)
			return false;
		
		if (!canAnnounce(ac))
			return false;
		
		currentPlayerAnnounced = true;
		
		if (ac.getAnnouncement() == Announcements.hkp)
		{
			idTrack.allIdentityRevealed();
		}
		else if (ac.getAnnouncement().requireIdentification())
		{
			idTrack.identityRevealed(player);
		}
		
		Team team = playerPairs.getTeam(player);
		announcementContraLevels.get(ac.getNextTeamToContra(team)).put(ac.getAnnouncement(), ac.getContraLevel());
		ac.getAnnouncement().onAnnounce(this);
		
		return true;
	}
	
	public boolean passz(int player)
	{
		if (isFinished())
			return false;
		
		if (player != currentPlayer)
			return false;
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			return false;
		}
		
		if (currentPlayerAnnounced)
		{
			lastAnnouncer = currentPlayer;
		}
		currentPlayer++;
		currentPlayer %= 4;
		currentPlayerAnnounced = false;
		
		return true;
	}
	
	public void clearAnnouncement(Team team, Announcement announcement)
	{
		announcementContraLevels.get(team).remove(announcement);
	}
	
	public List<AnnouncementContra> getAvailableAnnouncements()
	{
		if (isFinished())
			return null;
		
		List<AnnouncementContra> result = new ArrayList<AnnouncementContra>();
		
		Team currentPlayerTeam = playerPairs.getTeam(currentPlayer);
		boolean needsIdentification = needsIdentification();
		
		for (Team origAnnouncer : Team.values())
		{
			for (Announcement a : Announcements.getAll())
			{
				if (isAnnounced(origAnnouncer, a))
				{
					if (a.canContra())
					{
						AnnouncementContra ac = new AnnouncementContra(a, getContraLevel(origAnnouncer, a) + 1);
						if (ac.getContraLevel() < 7 && ac.getNextTeamToContra(origAnnouncer) == currentPlayerTeam)
						{
							result.add(ac);
						}
					}
				}
				else
				{
					if ((!needsIdentification || !a.requireIdentification()) && origAnnouncer == currentPlayerTeam && a.canBeAnnounced(this))
					{
						result.add(new AnnouncementContra(a, 0));
					}
				}
			}
		}
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			result.remove(new AnnouncementContra(Announcements.jatek, 1));
		}
		
		return result;
	}
	
	public boolean canAnnounce(AnnouncementContra ac)
	{
		Team currentPlayerTeam = playerPairs.getTeam(currentPlayer);
		Announcement a = ac.getAnnouncement();
		
		if (ac.equals(new AnnouncementContra(Announcements.jatek, 1)) && Announcements.hkp.canBeAnnounced(this))
			return false;
		
		if (ac.getContraLevel() == 0)
		{
			return (!needsIdentification() || !a.requireIdentification()) && a.canBeAnnounced(this);
		}
		else
		{
			Team originalAnnouncer = ac.getNextTeamToContra(currentPlayerTeam);
			
			return a.canContra() &&
			       ac.getContraLevel() < 7 &&
			       isAnnounced(originalAnnouncer, a) &&
			       ac.getContraLevel() == getContraLevel(originalAnnouncer, a) + 1;
		}
	}
	
	private boolean needsIdentification()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (lastAnnouncer < 0)
			return false;
		
		Team currentPlayerTeam = playerPairs.getTeam(currentPlayer);
		Team lastAnnouncerTeam = playerPairs.getTeam(lastAnnouncer);
		
		return currentPlayerTeam != lastAnnouncerTeam && !idTrack.isIdentityKnown(currentPlayer);
	}
	
	public int getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	public Team getCurrentTeam()
	{
		return playerPairs.getTeam(currentPlayer);
	}
	
	public boolean isAnnounced(Team team, Announcement a)
	{
		return announcementContraLevels.get(team).containsKey(a);
	}
	
	public int getContraLevel(Team team, Announcement a)
	{
		Integer contraLevel = announcementContraLevels.get(team).get(a);
		if (contraLevel == null)
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
	
	public int getPlayerToAnnounceSolo()
	{
		return playerToAnnounceSolo;
	}

	public boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private static class IdentityTracker
	{
		private final PlayerPairs playerPairs;
		private boolean[] identityKnown = new boolean[4];
		
		public IdentityTracker(PlayerPairs pp, Invitation invitAccepted)
		{
			playerPairs = pp;
			identityKnown[playerPairs.getCaller()] = true;
			if (invitAccepted != Invitation.NONE)
			{
				allIdentityRevealed();
			}
		}
		
		public void identityRevealed(int player)
		{
			if (player == playerPairs.getCalled() && !playerPairs.isSolo())
			{
				Arrays.fill(identityKnown, true);
			}
			identityKnown[player] = true;
		}
		
		public void allIdentityRevealed()
		{
			Arrays.fill(identityKnown, true);
		}
		
		public boolean isIdentityKnown(int player)
		{
			return identityKnown[player];
		}
	}
}