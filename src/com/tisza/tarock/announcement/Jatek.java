package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Jatek extends GamePoints
{
	public Result isSuccessful(GameInstance gi, Team team)
	{
		int pointsForDupla = Announcements.dupla.calculatePoints(gi, team) + Announcements.hosszuDupla.calculatePoints(gi, team);
		boolean isContraJatek = gi.announcing.isAnnounced(team, this) && gi.announcing.getContraLevel(team, this) > 0;
		
		if (pointsForDupla > 0 && !isContraJatek)
			return Result.DEACTIVATED;
		
		return super.isSuccessful(gi, team);
	}
	
	public boolean canBeAnnounced(Announcing announcing)
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
