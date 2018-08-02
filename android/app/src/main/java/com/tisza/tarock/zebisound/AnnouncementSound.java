package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;

class AnnouncementSound extends ZebiSound
{
	private final Announcement announcement;
	private final int[] audioResources;

	public AnnouncementSound(Context context, Announcement announcement, int ... audioResources)
	{
		super(context);
		this.announcement = announcement;
		this.audioResources = audioResources;
	}

	@Override
	protected int getAudioRes()
	{
		return audioResources[rnd.nextInt(audioResources.length)];
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (this.announcement.equals(announcement))
			activate();
	}
}
