package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Jatek extends GamePoints
{
	public Result isSuccessful(GameInstance gi, Team team)
	{
		boolean duplaAnnounced = gi.announcing.isAnnounced(team, Announcements.dupla);
		boolean isContraJatek = gi.announcing.isAnnounced(team, this) && gi.announcing.getContraLevel(team, this) > 0;
		
		if (duplaAnnounced && !isContraJatek)
			return Result.DEACTIVATED;
		
		return super.isSuccessful(gi, team);
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (team != Team.CALLER)
			return false;
		
		return super.canBeAnnounced(announcing, team);
	}

	protected int getMinPointsRequired()
	{
		return 48;
	}

	protected int getDefaultPoints()
	{
		return 1;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
