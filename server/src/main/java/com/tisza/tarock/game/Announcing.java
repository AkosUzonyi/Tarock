package com.tisza.tarock.game;

import com.tisza.tarock.announcement.Announcement;
import com.tisza.tarock.announcement.Announcements;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.Bidding.Invitation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Announcing extends Phase implements IAnnouncing
{
	private int currentPlayer;
	private boolean currentPlayerAnnounced = false;
	private int lastAnnouncer = -1;
	private IdentityTracker idTrack;
	
	public Announcing(GameSession gameSession)
	{
		super(gameSession);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.ANNOUNCING;
	}
	
	public void onStart()
	{
		currentPlayer = currentGame.getPlayerPairs().getCaller();
		idTrack = new IdentityTracker(currentGame.getPlayerPairs(), currentGame.getInvitAccepted());
		
		announce(currentPlayer, Announcements.jatek);
		
		sendAvailableAnnouncements();
	}

	public void announce(int player, Announcement a)
	{
		announce(player, new AnnouncementContra(a, 0));
	}

	public void announce(int player, AnnouncementContra ac)
	{
		if (player != currentPlayer)
			return;
		
		if (!canAnnounce(ac))
			return;
		
		currentPlayerAnnounced = true;
		
		if (ac.getAnnouncement() == Announcements.hkp)
		{
			idTrack.allIdentityRevealed();
		}
		else if (ac.getAnnouncement().requireIdentification())
		{
			idTrack.identityRevealed(player);
		}
		
		Team team = currentGame.getPlayerPairs().getTeam(player);
		setContraLevel(ac.getNextTeamToContra(team), ac.getAnnouncement(), ac.getContraLevel());
		ac.getAnnouncement().onAnnounced(this);

		gameSession.getBroadcastEventSender().announce(player, ac);
		sendAvailableAnnouncements();
	}
	
	public void announcePassz(int player)
	{
		if (player != currentPlayer)
			return;
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			//gameSession.sendEvent(player, new EventActionFailed(Reason.CONTRAJATEK_REQUIRED));
			return;
		}
		
		if (currentPlayerAnnounced)
		{
			lastAnnouncer = currentPlayer;
		}
		currentPlayer++;
		currentPlayer %= 4;
		currentPlayerAnnounced = false;
		
		if (!isFinished())
		{
			sendAvailableAnnouncements();
		}
		else
		{
			gameSession.changePhase(new Gameplay(gameSession));
		}

		gameSession.getBroadcastEventSender().announcePassz(player);
	}
	
	private boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private void sendAvailableAnnouncements()
	{
		List<AnnouncementContra> list = new ArrayList<AnnouncementContra>();
		
		Team currentPlayerTeam = currentGame.getPlayerPairs().getTeam(currentPlayer);
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
							list.add(ac);
						}
					}
				}
				else
				{
					if ((!needsIdentification || !a.requireIdentification()) && origAnnouncer == currentPlayerTeam && a.canBeAnnounced(this) && a.isShownInList())
					{
						list.add(new AnnouncementContra(a, 0));
					}
				}
			}
		}
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			list.remove(new AnnouncementContra(Announcements.jatek, 1));
		}
		
		gameSession.getPlayerEventQueue(currentPlayer).availableAnnouncements(list);
		gameSession.getBroadcastEventSender().turn(currentPlayer);
	}
	
	public boolean canAnnounce(AnnouncementContra ac)
	{
		Team currentPlayerTeam = currentGame.getPlayerPairs().getTeam(currentPlayer);
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
		if (lastAnnouncer < 0)
			return false;
		
		Team currentPlayerTeam = currentGame.getPlayerPairs().getTeam(currentPlayer);
		Team lastAnnouncerTeam = currentGame.getPlayerPairs().getTeam(lastAnnouncer);
		
		return currentPlayerTeam != lastAnnouncerTeam && !idTrack.isIdentityKnown(currentPlayer);
	}

	public int getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	public Team getCurrentTeam()
	{
		return currentGame.getPlayerPairs().getTeam(currentPlayer);
	}

	public boolean isAnnounced(Team team, Announcement a)
	{
		return currentGame.getAnnouncementsState().isAnnounced(team, a);
	}

	public void setContraLevel(Team team, Announcement a, int level)
	{
		 currentGame.getAnnouncementsState().setContraLevel(team, a, level);
	}

	public int getContraLevel(Team team, Announcement a)
	{
		 return currentGame.getAnnouncementsState().getContraLevel(team, a);
	}

	public void clearAnnouncement(Team team, Announcement a)
	{
		 currentGame.getAnnouncementsState().clearAnnouncement(team, a);
	}

	public PlayerCards getCards(int player)
	{
		return currentGame.getPlayerCards(player);
	}

	public int getPlayerToAnnounceSolo()
	{
		return currentGame.getPlayerToAnnounceSolo();
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
