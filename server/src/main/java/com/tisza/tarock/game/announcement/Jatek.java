package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class Jatek extends AnnouncementBase
{
	@Override
	public String getID()
	{
		return "jatek";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		Team teamEarningPoints = game.calculateCardPoints(team) >= 48 ? team : team.getOther();

		int pointsForDupla = Announcements.dupla.calculatePoints(game, teamEarningPoints);
		int pointsForVolat = Announcements.volat.calculatePoints(game, teamEarningPoints);
		boolean isContraJatek = game.getAnnouncementsState().isAnnounced(team, this) && game.getAnnouncementsState().getContraLevel(team, this) > 0;

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
