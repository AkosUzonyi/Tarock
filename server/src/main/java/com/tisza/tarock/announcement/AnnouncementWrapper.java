package com.tisza.tarock.announcement;

import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public abstract class AnnouncementWrapper implements Announcement
{
	private final AnnouncementContra wrappedAnnouncement;
	
	public AnnouncementWrapper(Announcement wrappedAnnouncement)
	{
		this(new AnnouncementContra(wrappedAnnouncement, 0));
	}
	
	public AnnouncementWrapper(AnnouncementContra wrappedAnnouncement)
	{
		this.wrappedAnnouncement = wrappedAnnouncement;
	}

	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.canAnnounce(wrappedAnnouncement);
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	public void onAnnounced(IAnnouncing announcing)
	{
		announcing.setContraLevel(announcing.getCurrentTeam(), wrappedAnnouncement.getAnnouncement(), wrappedAnnouncement.getContraLevel());
	}

	public boolean canContra()
	{
		return false;
	}

	public boolean isShownInList()
	{
		return true;
	}

	public boolean requireIdentification()
	{
		return true;
	}
}
