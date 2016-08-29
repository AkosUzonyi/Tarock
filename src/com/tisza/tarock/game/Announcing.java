package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;

public class Announcing
{
	private final PlayerPairs playerPairs;
	private int currentPlayer;
	
	private int lastAnnouncer = -1;
	private IdentityTracker idTrack;
	
	private Map<Announcement, AnnouncementState> announcementStates = new HashMap<Announcement, AnnouncementState>();
	
	private AllPlayersCards playerHands;
	
	public Announcing(PlayerPairs pp)
	{
		playerPairs = pp;
		currentPlayer = playerPairs.getCaller();
		idTrack = new IdentityTracker(playerPairs);
		for (Announcement a : Announcements.getAll())
		{
			announcementStates.put(a, new AnnouncementState());
		}
	}
	
	public boolean announce(int player, List<Announcement> announcements)
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (player != currentPlayer)
			return false;
		
		if (!announcements.isEmpty() && !idTrack.isIdentityKnown(player))
			return false;
		
		if (lastAnnouncer < 0)
		{
			//announcements.add(game);
		}
		
		Team team = playerPairs.getTeam(player);
		
		for (Announcement a : announcements)
		{
			announcementStates.get(a).team(team).announce();
			idTrack.identityRevealed(player);
		}
		
		if (!announcements.isEmpty()) lastAnnouncer = currentPlayer;
		
		currentPlayer++;
		return true;
	}
	
	public boolean contra(int player, List<Contra> contraList)
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (player != currentPlayer)
			return false;
		
		Team playerTeam = playerPairs.getTeam(player);
		for (Contra c : contraList)
		{
			Team announcerTeam = c.isSelf() ? playerTeam : playerTeam.getOther();
			AnnouncementState.PerTeam as = announcementStates.get(c.getAnnouncement()).team(announcerTeam);
			if (as.isAnnounced() && as.getNextTeamToContra() == playerTeam)
			{
				as.contra();
				idTrack.identityRevealed(player);
			}
		}
		return true;
	}
	
	public boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private static class IdentityTracker
	{
		private final PlayerPairs playerPairs;
		
		public IdentityTracker(PlayerPairs pp)
		{
			playerPairs = pp;
		}
		
		public void identityRevealed(int player)
		{
		}
		
		public boolean isIdentityKnown(int player)
		{
			return false;
		}
	}
}