package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIFogas extends AnnouncementBase
{
	XXIFogas(){}

	@Override
	public String getName()
	{
		return "xxifogas";
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		PlayerPairs pp = gameState.getPlayerPairs();
		
		for (int i = 0; i < GameSession.ROUND_COUNT; i++)
		{
			Round round = gameState.getRound(i);
			PlayerSeat skizPlayer = round.getPlayerOfCard(Card.getTarockCard(22));
			PlayerSeat XXIPlayer = round.getPlayerOfCard(Card.getTarockCard(21));
			if (skizPlayer == null || XXIPlayer == null) continue;
			
			Team skizTeam = pp.getTeam(skizPlayer);
			Team XXITeam = pp.getTeam(XXIPlayer);
			if (skizTeam == team && XXITeam != team)
			{
				return Result.SUCCESSFUL_SILENT;
			}
		}
		return Result.FAILED;
	}

	@Override
	public int getPoints()
	{
		return 60;
	}
}
