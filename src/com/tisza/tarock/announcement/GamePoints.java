package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

abstract class GamePoints extends AnnouncementBase
{
	GamePoints(){}
	
	protected abstract int getMinPointsRequired();
	protected abstract int getDefaultPoints();

	protected boolean canBeSilent()
	{
		return true;
	}
	
	public Result isSuccessful(Gameplay gp, PlayerPairs pp, Team team)
	{
		int points = 0;
		for (int player : pp.getPlayersInTeam(team))
		{
			for (Card c : gp.getWonCards(player))
			{
				points += c.getPoints();
			}
		}
		return points > getMinPointsRequired() ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
	
	protected int getPoints(int winnerBid)
	{
		return getDefaultPoints() * (4 - winnerBid);
	}
}
