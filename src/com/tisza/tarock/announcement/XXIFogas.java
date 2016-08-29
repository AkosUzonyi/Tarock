package com.tisza.tarock.announcement;

import com.tisza.tarock.announcement.AnnouncementBase.Result;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIFogas
{
	XXIFogas(){}
	
	public Result isSuccessful(Gameplay gp, PlayerPairs pp, Team team)
	{
		for (Round r : gp.getRoundsPassed())
		{
			int skizPlayer = r.getCards().indexOf(new TarockCard(22));
			int XXIPlayer = r.getCards().indexOf(new TarockCard(21));
			if (skizPlayer < 0 || XXIPlayer < 0) continue;
			
			Team skizTeam = pp.getTeam(skizPlayer);
			Team XXITeam = pp.getTeam(XXIPlayer);
			if (skizTeam == team && XXITeam != team)
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
