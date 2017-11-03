package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class GamePoints extends AnnouncementBase
{
	GamePoints(){}
	
	protected abstract int getMinPointsRequired();
	protected abstract boolean canBeSilent();
	
	public Result isSuccessful(GameState gameState, Team team)
	{
		return gameState.calculateGamePoints(team) >= getMinPointsRequired() ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}
}
