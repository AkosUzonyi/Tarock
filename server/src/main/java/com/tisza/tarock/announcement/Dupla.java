package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Dupla extends AnnouncementBase
{
	@Override
	public String getName()
	{
		return "dupla";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	protected Result isSuccessful(GameState gameState, Team team)
	{
		if (gameState.calculateGamePoints(team) < 71)
			return Result.FAILED;

		boolean canBeSilent = Announcements.volat.calculatePoints(gameState, team) == 0;

		return canBeSilent ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.volat))
			return false;
		
		return super.canBeAnnounced(announcing);
	}

	@Override
	protected boolean isMultipliedByWinnerBid()
	{
		return true;
	}

	@Override
	protected int getPoints()
	{
		return 4;
	}
}
