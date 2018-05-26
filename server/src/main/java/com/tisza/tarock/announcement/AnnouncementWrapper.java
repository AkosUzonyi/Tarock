package com.tisza.tarock.announcement;

import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.GameState;
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

	@Override
	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.canAnnounce(wrappedAnnouncement);
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		announcing.setContraLevel(announcing.getCurrentTeam(), wrappedAnnouncement.getAnnouncement(), wrappedAnnouncement.getContraLevel());
	}

	@Override
	public boolean canContra()
	{
		return false;
	}

	@Override
	public boolean isShownInList()
	{
		return true;
	}

	@Override
	public boolean requireIdentification()
	{
		return wrappedAnnouncement.getContraLevel() == 0;
	}
}
