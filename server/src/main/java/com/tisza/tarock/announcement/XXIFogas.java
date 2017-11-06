package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIFogas extends AnnouncementBase
{
	XXIFogas(){}
	
	public Result isSuccessful(GameState gameState, Team team)
	{
		PlayerPairs pp = gameState.getPlayerPairs();
		
		for (int i = 0; i < GameState.ROUND_COUNT; i++)
		{
			Round round = gameState.getRound(i);
			int skizPlayer = round.getPlayerOfCard(new TarockCard(22));
			int XXIPlayer = round.getPlayerOfCard(new TarockCard(21));
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
}
