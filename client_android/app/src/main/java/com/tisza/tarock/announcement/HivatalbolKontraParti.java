package com.tisza.tarock.announcement;

import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class HivatalbolKontraParti extends AnnouncementBridge
{
	public HivatalbolKontraParti()
	{
		super(new AnnouncementContra(Announcements.jatek, 1));
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		if (announcing.isAnnounced(announcing.getCurrentTeam(), Announcements.hkp))
			return false;
		
		if (announcing.getCurrentPlayer() != announcing.getPlayerToAnnounceSolo())
			return false;
		
		return announcing.getContraLevel(Team.CALLER, Announcements.jatek) == 0;
	}

	public boolean requireIdentification()
	{
		return false;
	}
}
