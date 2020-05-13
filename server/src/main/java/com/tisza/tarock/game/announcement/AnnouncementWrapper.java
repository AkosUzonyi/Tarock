package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class AnnouncementWrapper implements Announcement
{
	private final AnnouncementContra wrappedAnnouncement;
	
	AnnouncementWrapper(Announcement wrappedAnnouncement)
	{
		this(new AnnouncementContra(wrappedAnnouncement, 0));
	}
	
	AnnouncementWrapper(AnnouncementContra wrappedAnnouncement)
	{
		this.wrappedAnnouncement = wrappedAnnouncement;
	}

	@Override
	public int calculatePoints(Game game, Team team)
	{
		return 0;
	}

	@Override
	public GameType getGameType()
	{
		return wrappedAnnouncement.getAnnouncement().getGameType();
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.canAnnounce(wrappedAnnouncement);
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = wrappedAnnouncement.getNextTeamToContra(announcing.getCurrentTeam());
		announcing.setContraLevel(team, wrappedAnnouncement.getAnnouncement(), wrappedAnnouncement.getContraLevel());
		wrappedAnnouncement.getAnnouncement().onAnnounced(announcing);
	}

	@Override
	public boolean canContra(IAnnouncing announcing)
	{
		return false;
	}

	@Override
	public boolean requireIdentification()
	{
		return wrappedAnnouncement.getContraLevel() == 0;
	}

	@Override
	public boolean shouldBeStored()
	{
		return false;
	}
}
