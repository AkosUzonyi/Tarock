package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Announcing extends Phase implements IAnnouncing
{
	private PlayerSeat currentPlayer;
	private boolean currentPlayerAnnounced = false;
	private PlayerSeat lastAnnouncer = null;

	public Announcing(GameState game)
	{
		super(game);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.ANNOUNCING;
	}
	
	@Override
	public void onStart()
	{
		currentPlayer = game.getPlayerPairs().getCaller();

		announce(currentPlayer, Announcements.jatek);
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		game.getPlayerEventSender(player).turn(currentPlayer);
		if (player == currentPlayer)
			sendAvailableAnnouncements();
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

		if (ac.getAnnouncement().shouldBeStored())
		{
			setContraLevel(ac.getNextTeamToContra(getCurrentTeam()), ac.getAnnouncement(), ac.getContraLevel());
		}

		history.registerAnnouncement(player, ac);
		ac.getAnnouncement().onAnnounced(this);
		game.getBroadcastEventSender().announce(player, ac);
		sendAvailableAnnouncements();
	}
	
	@Override
	public void announcePassz(PlayerSeat player)
	{
		if (player != currentPlayer)
			return;
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			//game.sendEvent(player, new EventActionFailed(Reason.CONTRAJATEK_REQUIRED));
			return;
		}
		
		if (currentPlayerAnnounced)
		{
			lastAnnouncer = currentPlayer;
		}
		currentPlayer = currentPlayer.nextPlayer();
		currentPlayerAnnounced = false;

		game.getBroadcastEventSender().announcePassz(player);

		if (!isFinished())
		{
			sendAvailableAnnouncements();
		}
		else
		{
			game.changePhase(new Gameplay(game));
		}
	}
	
	private boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}
	
	private void sendAvailableAnnouncements()
	{
		List<AnnouncementContra> list = new ArrayList<>();
		
		Team currentPlayerTeam = getCurrentTeam();
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
							game.getGameType().hasParent(a.getGameType())
						)
						list.add(new AnnouncementContra(a, 0));
				}
			}
		}
		
		if (Announcements.hkp.canBeAnnounced(this))
		{
			list.remove(new AnnouncementContra(Announcements.jatek, 1));
		}
		
		game.getPlayerEventSender(currentPlayer).availableAnnouncements(list);
		game.getBroadcastEventSender().turn(currentPlayer);
	}
	
	@Override
	public boolean canAnnounce(AnnouncementContra ac)
	{
		Team currentPlayerTeam = getCurrentTeam();
		Announcement a = ac.getAnnouncement();
		
		if (ac.equals(new AnnouncementContra(Announcements.jatek, 1)) && Announcements.hkp.canBeAnnounced(this))
			return false;
		
		if (ac.getContraLevel() == 0)
		{
			return (!needsIdentification() || !a.requireIdentification()) &&
					a.canBeAnnounced(this) &&
					game.getGameType().hasParent(a.getGameType());
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
		
		Team currentPlayerTeam = game.getPlayerPairs().getTeam(currentPlayer);
		Team lastAnnouncerTeam = game.getPlayerPairs().getTeam(lastAnnouncer);
		
		return currentPlayerTeam != lastAnnouncerTeam && !game.getTeamInfoTracker().isTeamInfoGlobalOf(currentPlayer);
	}

	@Override
	public PlayerSeat getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	@Override
	public Team getCurrentTeam()
	{
		return game.getPlayerPairs().getTeam(currentPlayer);
	}

	@Override
	public PlayerPairs getPlayerPairs()
	{
		return game.getPlayerPairs();
	}

	@Override
	public boolean isAnnounced(Team team, Announcement a)
	{
		return game.getAnnouncementsState().isAnnounced(team, a);
	}

	@Override
	public void setContraLevel(Team team, Announcement a, int level)
	{
		 game.getAnnouncementsState().setContraLevel(team, a, level);
	}

	@Override
	public int getContraLevel(Team team, Announcement a)
	{
		 return game.getAnnouncementsState().getContraLevel(team, a);
	}

	@Override
	public void clearAnnouncement(Team team, Announcement a)
	{
		 game.getAnnouncementsState().clearAnnouncement(team, a);
	}

	@Override
	public void setXXIUltimoDeactivated(Team team)
	{
		game.getAnnouncementsState().setXXIUltimoDeactivated(team);
	}

	public void announceTarockCount(PlayerSeat player, TarockCount announcement)
	{
		game.getAnnouncementsState().announceTarockCount(player, announcement);
	}

	public TarockCount getTarockCountAnnounced(PlayerSeat player)
	{
		return game.getAnnouncementsState().getTarockCountAnnounced(player);
	}

	@Override
	public boolean getXXIUltimoDeactivated(Team team)
	{
		return game.getAnnouncementsState().getXXIUltimoDeactivated(team);
	}

	@Override
	public PlayerCards getCards(PlayerSeat player)
	{
		return game.getPlayerCards(player);
	}

	@Override
	public PlayerSeat getPlayerToAnnounceSolo()
	{
		return game.getPlayerToAnnounceSolo();
	}

	@Override
	public GameType getGameType()
	{
		return game.getGameType();
	}

}
