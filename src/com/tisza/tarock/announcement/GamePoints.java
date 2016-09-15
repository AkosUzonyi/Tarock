package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class GamePoints extends AnnouncementBase
{
	GamePoints(){}
	
	protected abstract int getMinPointsRequired();
	protected abstract boolean canBeSilent();
	
	public Result isSuccessful(GameInstance gi, Team team)
	{
		return calculateGamePoints(gi, team) >= getMinPointsRequired() ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}
	
	public static int calculateGamePoints(GameInstance gi, Team team)
	{
		int points = 0;
		for (int player : gi.calling.getPlayerPairs().getPlayersInTeam(team))
		{
			for (Card c : gi.gameplay.getWonCards(player))
			{
				points += c.getPoints();
			}
		}
		for (Card c : gi.changing.getSkartForTeam(team))
		{
			points += c.getPoints();
		}
		
		return points;
	}
}
