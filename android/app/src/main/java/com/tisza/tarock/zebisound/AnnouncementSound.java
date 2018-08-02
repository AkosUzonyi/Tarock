package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;

class AnnouncementSound extends ZebiSound
{
	private final Announcement announcement;

	public AnnouncementSound(Context context, Announcement announcement, int ... audioResources)
	{
		super(context, 1F, audioResources);
		this.announcement = announcement;
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (this.announcement.equals(announcement))
			activate();
	}
}
