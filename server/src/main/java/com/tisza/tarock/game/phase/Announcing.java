package com.tisza.tarock.game.phase;


import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.stream.*;

class Announcing extends Phase implements IAnnouncing
{
	private static final int MAX_CONTRA_LEVEL = 7;

	private PlayerSeat currentPlayer;
	private boolean currentPlayerAnnounced = false;
	private PlayerSeat lastAnnouncer = null;

	public Announcing(Game game)
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

		announce(currentPlayer, new AnnouncementContra(Announcements.jatek, 0));
	}

	@Override
	public boolean announce(PlayerSeat player, AnnouncementContra ac)
	{
		if (player != currentPlayer)
			return false;
		
		if (!canAnnounce(ac))
			return false;
		
		currentPlayerAnnounced = true;

		if (ac.getAnnouncement().shouldBeStored())
			setContraLevel(ac.getNextTeamToContra(getCurrentTeam()), ac.getAnnouncement(), ac.getContraLevel());

		if (ac.getContraLevel() == 0)
			ac.getAnnouncement().onAnnounced(this);

		if (ac.getAnnouncement() == Announcements.hkp)
			game.revealAllTeamInfo();

		if (ac.getAnnouncement().requireIdentification())
		{
			if (player == game.getPlayerPairs().getCalled() && !game.getPlayerPairs().isSolo())
				game.revealAllTeamInfo();
			else
				game.revealAllTeamInfoOf(player);
		}

		game.broadcastEvent(Event.announce(player, ac));
		sendAvailableAnnouncements();

		return true;
	}
	
	@Override
	public boolean announcePassz(PlayerSeat player)
	{
		if (player != currentPlayer)
			return false;
		
		if (shouldHkpBeAnnounced())
		{
			//game.sendEvent(player, new EventActionFailed(Reason.CONTRAJATEK_REQUIRED));
			return false;
		}
		
		if (currentPlayerAnnounced)
		{
			lastAnnouncer = currentPlayer;
		}
		currentPlayer = currentPlayer.nextPlayer();
		currentPlayerAnnounced = false;

		game.broadcastEvent(Event.announcePassz(player));

		if (!isFinished())
		{
			sendAvailableAnnouncements();
		}
		else
		{
			game.changePhase(new Gameplay(game));
		}

		return true;
	}
	
	private boolean isFinished()
	{
		return lastAnnouncer == currentPlayer;
	}

	private List<AnnouncementContra> getAvailableAnnouncements()
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
					if (a.canContra(this))
					{
						AnnouncementContra ac = new AnnouncementContra(a, getContraLevel(origAnnouncer, a) + 1);
						if (ac.getContraLevel() < MAX_CONTRA_LEVEL && ac.getNextTeamToContra(origAnnouncer) == currentPlayerTeam)
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
		
		if (shouldHkpBeAnnounced())
			list.remove(new AnnouncementContra(Announcements.jatek, 1));

		return list;
	}

	private void sendAvailableAnnouncements()
	{
		game.sendEvent(currentPlayer, Event.availableAnnouncements(getAvailableAnnouncements()));
		game.turn(currentPlayer);
	}

	@Override
	public List<Action> getAvailableActions()
	{
		return getAvailableAnnouncements().stream().map(Action::announce).collect(Collectors.toList());
	}

	@Override
	public boolean canAnnounce(AnnouncementContra ac)
	{
		Team currentPlayerTeam = getCurrentTeam();
		Announcement a = ac.getAnnouncement();

		if (ac.equals(new AnnouncementContra(Announcements.jatek, 1)) && shouldHkpBeAnnounced())
			return false;

		if (ac.getContraLevel() == 0)
		{
			return (!needsIdentification() || !a.requireIdentification()) &&
					!isAnnounced(currentPlayerTeam, a) &&
					a.canBeAnnounced(this) &&
					game.getGameType().hasParent(a.getGameType());
		}
		else
		{
			Team originalAnnouncer = ac.getNextTeamToContra(currentPlayerTeam);

			return a.canContra(this) &&
			       ac.getContraLevel() < MAX_CONTRA_LEVEL &&
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
		
		return currentPlayerTeam != lastAnnouncerTeam && !game.isTeamInfoGlobalOf(currentPlayer);
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

	@Override
	public void announceTarockCount(PlayerSeat player, TarockCount announcement)
	{
		game.getAnnouncementsState().announceTarockCount(player, announcement);
	}

	@Override
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
	public boolean shouldHkpBeAnnounced()
	{
		if (isAnnounced(getCurrentTeam(), Announcements.hkp))
			return false;

		if (getContraLevel(Team.CALLER, Announcements.jatek) > 0)
			return false;

		return game.getSkart(currentPlayer).contains(game.getCalledCard());
	}

	@Override
	public GameType getGameType()
	{
		return game.getGameType();
	}

}
