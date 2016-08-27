package com.tisza.tarock.announcement;

import com.tisza.tarock.announcement.AnnouncementBase.Result;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIFogas
{
	public Result isSuccessful(Gameplay gp, PlayerPairs pp, boolean callerTeam)
	{
		for (Round r : gp.getRoundsPassed())
		{
			int skizPlayer = r.getCards().indexOf(new TarockCard(22));
			int XXIPlayer = r.getCards().indexOf(new TarockCard(21));
			if (skizPlayer < 0 || XXIPlayer < 0) continue;
			
			boolean skizCaller = pp.isCallerTeam(skizPlayer);
			boolean XXICaller = pp.isCallerTeam(XXIPlayer);
			if (skizCaller == callerTeam && XXICaller != callerTeam)
			{
				return Result.SUCCESSFUL_SILENT;
			}
		}
		return Result.FAILED;
	}

	public int getPoints()
	{
		return 60;
	}
	
	public int getID()
	{
		return 2;
	}
}
