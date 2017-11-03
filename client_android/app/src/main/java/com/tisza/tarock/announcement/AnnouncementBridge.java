package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBridge implements Announcement
{
	private final AnnouncementContra bridgedAnnouncement;
	
	public AnnouncementBridge(Announcement bridgedAnnouncement)
	{
		this(new AnnouncementContra(bridgedAnnouncement, 0));
	}
	
	public AnnouncementBridge(AnnouncementContra bridgedAnnouncement)
	{
		this.bridgedAnnouncement = bridgedAnnouncement;
	}

	public int calculatePoints(GameState gameState, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.canAnnounce(bridgedAnnouncement);
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	public void onAnnounce(IAnnouncing announcing)
	{
		announcing.setContraLevel(announcing.getCurrentTeam(), bridgedAnnouncement.getAnnouncement(), bridgedAnnouncement.getContraLevel());
	}

	public boolean canContra()
	{
		return false;
	}

	public boolean isShownToUser()
	{
		return true;
	}

	public boolean requireIdentification()
	{
		return true;
	}
}
