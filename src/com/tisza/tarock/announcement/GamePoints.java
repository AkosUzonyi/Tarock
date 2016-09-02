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
	
	public Result isSuccessful(GameInstance gi, Team team)
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
		System.out.println(points);
		
		return points >= getMinPointsRequired() ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
	
	public int getPoints(int winnerBid)
	{
		return getDefaultPoints() * (4 - winnerBid);
	}
}
