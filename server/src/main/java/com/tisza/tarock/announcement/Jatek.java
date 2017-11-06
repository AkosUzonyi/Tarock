package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Jatek extends GamePoints
{
	public Result isSuccessful(GameState gameState, Team team)
	{
		int pointsForDupla = Announcements.dupla.calculatePoints(gameState, team);
		boolean isContraJatek = gameState.getAnnouncementsState().isAnnounced(team, this) && gameState.getAnnouncementsState().getContraLevel(team, this) > 0;
		
		if (pointsForDupla > 0 && !isContraJatek)
			return Result.DEACTIVATED;
		
		return super.isSuccessful(gameState, team);
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (team != Team.CALLER)
			return false;
		
		return super.canBeAnnounced(announcing);
	}

	protected int getMinPointsRequired()
	{
		return 48;
	}

	protected int getPoints()
	{
		return 1;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
