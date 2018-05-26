package com.tisza.tarock.announcement;

import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Team;

public class Jatek extends AnnouncementBase
{
	@Override
	public String getName()
	{
		return "jatek";
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		Team teamEarningPoints = gameState.calculateGamePoints(team) >= 48 ? team : team.getOther();

		int pointsForDupla = Announcements.dupla.calculatePoints(gameState, teamEarningPoints);
		int pointsForVolat = Announcements.volat.calculatePoints(gameState, teamEarningPoints);
		boolean isContraJatek = gameState.getAnnouncementsState().isAnnounced(team, this) && gameState.getAnnouncementsState().getContraLevel(team, this) > 0;

		if (!isContraJatek && (pointsForVolat != 0 || pointsForDupla != 0))
			return Result.DEACTIVATED;

		return team == teamEarningPoints ? Result.SUCCESSFUL : Result.FAILED;
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (team != Team.CALLER)
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
		return 1;
	}
}
