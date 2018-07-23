package com.tisza.tarock.game;

import com.tisza.tarock.game.announcement.*;

import java.util.*;

public class AnnouncementsState
{
	private Map<Team, Map<Announcement, Integer>> announcementContraLevels = new HashMap<>();

	private boolean callerXXIUltimoDeactivated = false, opponentXIUltimoDeactivated = false;
	private PlayerSeat.Map<TarockCount> tarockCountAnnounced = new PlayerSeat.Map<>(null);

	public AnnouncementsState()
	{
		for (Team team : Team.values())
		{
			Map<Announcement, Integer> map = new HashMap<>();
			announcementContraLevels.put(team, map);
		}
	}
	
	public boolean isAnnounced(Team team, Announcement a)
	{
		return announcementContraLevels.get(team).containsKey(a);
	}
	
	public void setContraLevel(Team team, Announcement a, int level)
	{
		announcementContraLevels.get(team).put(a, level);
	}
	
	public int getContraLevel(Team team, Announcement a)
	{
		Integer contraLevel = announcementContraLevels.get(team).get(a);
		if (contraLevel == null)
			throw new IllegalStateException();
		return contraLevel;
	}
	
	public void clearAnnouncement(Team team, Announcement announcement)
	{
		announcementContraLevels.get(team).remove(announcement);
	}

	public void setXXIUltimoDeactivated(Team team)
	{
		if (team == Team.CALLER)
		{
			callerXXIUltimoDeactivated = true;
		}
		else
		{
			opponentXIUltimoDeactivated = true;
		}
	}

	public boolean getXXIUltimoDeactivated(Team team)
	{
		if (team == Team.CALLER)
		{
			return callerXXIUltimoDeactivated;
		}
		else
		{
			return opponentXIUltimoDeactivated;
		}
	}

	public void announceTarockCount(PlayerSeat player, TarockCount announcement)
	{
		tarockCountAnnounced.put(player, announcement);
	}

	public TarockCount getTarockCountAnnounced(PlayerSeat player)
	{
		return tarockCountAnnounced.get(player);
	}
}
