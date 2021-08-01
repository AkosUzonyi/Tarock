package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class DuplaFail extends ZebiSound
{
	public DuplaFail(Context context)
	{
		super(context, 1F, R.raw.duplatbovenelbuktatok);
	}

	/*@Override
	public void statistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
	{
		for (AnnouncementResult announcementResult : announcementResults)
		{
			int teamPoints = announcementResult.getTeam() == Team.CALLER ? callerGamePoints : opponentGamePoints;
			if (announcementResult.getAnnouncement().getName().equals("dupla") && announcementResult.getPoints() < 0 && teamPoints < 65)
				activate();
		}
	}*/
}
