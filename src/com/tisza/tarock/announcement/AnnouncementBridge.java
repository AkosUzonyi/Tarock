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

	public int calculatePoints(GameInstance gi, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(Announcing announcing)
	{
		return announcing.canAnnounce(bridgedAnnouncement);
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	public void onAnnounce(Announcing announcing)
	{
		announcing.announce(announcing.getCurrentPlayer(), bridgedAnnouncement);
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
