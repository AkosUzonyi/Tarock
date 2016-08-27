package com.tisza.tarock.game;

import java.security.acl.*;
import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;

public class Announcing
{
	private final PlayerPairs playerPairs;
	private final int currentPlayer;
	
	private int lastAnnouncer = -1;
	private boolean[] identityKnown = new boolean[4];
	
	private Collection<AnnouncementInstance> allAnnouncements = new ArrayList<AnnouncementInstance>();
	private PairState callerState = new PairState();
	private PairState oppponentState = new PairState();
	
	private AllPlayersCards playerHands;
	
	public Announcing(PlayerPairs pp)
	{
		playerPairs = pp;
		currentPlayer = playerPairs.getCaller();
		identityKnown[currentPlayer] = true;
	}
	
	public boolean announce(int player, List<Announcement> announcements)
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (player != currentPlayer)
			return false;
		
		if (announcements.isEmpty())
			return true;
		
		if (!identityKnown[currentPlayer] && playerPairs.isCallerTeam(player) != playerPairs.isCallerTeam(lastAnnouncer))
			return false;
		
		boolean inCallerTeam = playerPairs.isCallerTeam(player);
		PairState thisPlayerState = inCallerTeam ? callerState : oppponentState;
		
		for (Announcement a : announcements)
		{
			thisPlayerState.announcements.add(a);
			allAnnouncements.add(new AnnouncementInstance(a, playerPairs, inCallerTeam));
		}
		
		lastAnnouncer = currentPlayer;
		
		return true;
	}
	
	public boolean contra(int player, List<Announcement> announcementsToContra)
	{
		if (isFinished())
			throw new IllegalStateException();
		
		if (player != currentPlayer)
			return false;
		
		for (Announcement a : announcementsToContra)
		{
			for (AnnouncementInstance ai : allAnnouncements)
			{
				if (ai.isCallerTeam() != playerPairs.isCallerTeam(player) && ai.getAnnouncement() == a)
				{
					ai.contra();
					identityKnown[player] = true;
					//TODO
				}
			}
		}
		return false;
	}
	
	public boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private static class PairState
	{
		public Collection<Announcement> announcements = new ArrayList<Announcement>();
		public boolean hasBanda = false;
		
	}
}