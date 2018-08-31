package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class AnnouncementSuccecfulSound extends ZebiSound
{
	private final Announcement announcement;

	public AnnouncementSuccecfulSound(Context context, Announcement announcement, int ... audioResources)
	{
		super(context, 1F, audioResources);
		this.announcement = announcement;
	}

	@Override
	public void statistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, List<Integer> points, int pointMultiplier)
	{
		teamStatistics(selfGamePoints, selfEntries);
		teamStatistics(opponentGamePoints, opponentEntries);
	}

	private void teamStatistics(int points, List<AnnouncementStaticticsEntry> entries)
	{
		for (AnnouncementStaticticsEntry entry : entries)
		{
			if (entry.getAnnouncement().equals(announcement) && entry.getPoints() > 0)
				activate();
		}
	}
}
