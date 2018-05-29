package com.tisza.tarock.game;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.*;

import java.util.*;

class Announcing extends Phase implements IAnnouncing
{
	private PlayerSeat currentPlayer;
	private boolean currentPlayerAnnounced = false;
	private PlayerSeat lastAnnouncer = null;
	private IdentityTracker idTrack;

	public Announcing(GameSession gameSession)
	{
		super(gameSession);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.ANNOUNCING;
	}
	
	@Override
	public void onStart()
	{
		currentPlayer = currentGame.getPlayerPairs().getCaller();
		idTrack = new IdentityTracker(currentGame.getPlayerPairs(), currentGame.getInvitAccepted());
		
		announce(currentPlayer, Announcements.jatek);
	}

	public void announce(PlayerSeat player, Announcement a)
	{
		announce(player, new AnnouncementContra(a, 0));
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra ac)
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

		if (ac.getAnnouncement().shouldBeStored())
		{
			Team team = currentGame.getPlayerPairs().getTeam(player);
			setContraLevel(ac.getNextTeamToContra(team), ac.getAnnouncement(), ac.getContraLevel());
		}

		ac.getAnnouncement().onAnnounced(this);

		gameSession.getBroadcastEventSender().announce(player, ac);
		sendAvailableAnnouncements();
	}
	
	@Override
	public void announcePassz(PlayerSeat player)
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
		currentPlayer = currentPlayer.nextPlayer();
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
					if ((!needsIdentification || !a.requireIdentification()) &&
							origAnnouncer == currentPlayerTeam &&
							a.canBeAnnounced(this) &&
							a.isShownInList() &&
							gameSession.getGameType().hasParent(a.getGameType())
						)
						list.add(new AnnouncementContra(a, 0));
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
	
	@Override
	public boolean canAnnounce(AnnouncementContra ac)
	{
		Team currentPlayerTeam = currentGame.getPlayerPairs().getTeam(currentPlayer);
		Announcement a = ac.getAnnouncement();
		
		if (ac.equals(new AnnouncementContra(Announcements.jatek, 1)) && Announcements.hkp.canBeAnnounced(this))
			return false;
		
		if (ac.getContraLevel() == 0)
		{
			return (!needsIdentification() || !a.requireIdentification()) &&
					a.canBeAnnounced(this) &&
					gameSession.getGameType().hasParent(a.getGameType());
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
		if (lastAnnouncer == null)
			return false;
		
		Team currentPlayerTeam = currentGame.getPlayerPairs().getTeam(currentPlayer);
		Team lastAnnouncerTeam = currentGame.getPlayerPairs().getTeam(lastAnnouncer);
		
		return currentPlayerTeam != lastAnnouncerTeam && !idTrack.isIdentityKnown(currentPlayer);
	}

	@Override
	public PlayerSeat getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	@Override
	public Team getCurrentTeam()
	{
		return currentGame.getPlayerPairs().getTeam(currentPlayer);
	}

	@Override
	public PlayerPairs getPlayerPairs()
	{
		return currentGame.getPlayerPairs();
	}

	@Override
	public boolean isAnnounced(Team team, Announcement a)
	{
		return currentGame.getAnnouncementsState().isAnnounced(team, a);
	}

	@Override
	public void setContraLevel(Team team, Announcement a, int level)
	{
		 currentGame.getAnnouncementsState().setContraLevel(team, a, level);
	}

	@Override
	public int getContraLevel(Team team, Announcement a)
	{
		 return currentGame.getAnnouncementsState().getContraLevel(team, a);
	}

	@Override
	public void clearAnnouncement(Team team, Announcement a)
	{
		 currentGame.getAnnouncementsState().clearAnnouncement(team, a);
	}

	@Override
	public void setXXIUltimoDeactivated(Team team)
	{
		currentGame.getAnnouncementsState().setXXIUltimoDeactivated(team);
	}

	public void announceTarockCount(PlayerSeat player, TarockCount announcement)
	{
		currentGame.getAnnouncementsState().announceTarockCount(player, announcement);
	}

	public TarockCount getTarockCountAnnounced(PlayerSeat player)
	{
		return currentGame.getAnnouncementsState().getTarockCountAnnounced(player);
	}

	@Override
	public boolean getXXIUltimoDeactivated(Team team)
	{
		return currentGame.getAnnouncementsState().getXXIUltimoDeactivated(team);
	}

	@Override
	public PlayerCards getCards(PlayerSeat player)
	{
		return currentGame.getPlayerCards(player);
	}

	@Override
	public PlayerSeat getPlayerToAnnounceSolo()
	{
		return currentGame.getPlayerToAnnounceSolo();
	}
	
	private static class IdentityTracker
	{
		private final PlayerPairs playerPairs;
		private PlayerSeat.Map<Boolean> identityKnown = new PlayerSeat.Map<>(false);
		
		public IdentityTracker(PlayerPairs pp, Invitation invitAccepted)
		{
			playerPairs = pp;
			if (invitAccepted != Invitation.NONE)
			{
				allIdentityRevealed();
			}
		}
		
		public void identityRevealed(PlayerSeat player)
		{
			if (player == playerPairs.getCalled() && !playerPairs.isSolo())
			{
				identityKnown.fill(true);
			}
			identityKnown.put(player, true);
		}
		
		public void allIdentityRevealed()
		{
			identityKnown.fill(true);
		}
		
		public boolean isIdentityKnown(PlayerSeat player)
		{
			return identityKnown.get(player);
		}
	}
}
