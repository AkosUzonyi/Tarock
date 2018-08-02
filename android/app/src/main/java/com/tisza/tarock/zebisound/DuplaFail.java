package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class DuplaFail extends ZebiSound
{
	public DuplaFail(Context context)
	{
		super(context);
	}

	@Override
	protected int getAudioRes()
	{
		return R.raw.duplatbovenelbuktatok;
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
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
			if (entry.getAnnouncement().getName().equals("dupla") && points < 65)
				activate();
		}
	}
}
