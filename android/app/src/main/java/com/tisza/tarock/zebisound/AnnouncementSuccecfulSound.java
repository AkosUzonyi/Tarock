package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;

public class AnnouncementSuccecfulSound extends ZebiSound
{
	private final Announcement announcement;

	public AnnouncementSuccecfulSound(Context context, Announcement announcement, int ... audioResources)
	{
		super(context, 1F, audioResources);
		this.announcement = announcement;
	}

	/*@Override
	public void statistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
	{
		for (AnnouncementResult announcementResult : announcementResults)
		{
			if (announcementResult.getAnnouncement().equals(announcement) && announcementResult.getPoints() > 0)
				activate();
		}
	}*/
}
