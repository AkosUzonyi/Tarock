package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public class Kisszincsalad extends Szincsalad
{
	Kisszincsalad(int suit)
	{
		super(suit);
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		for (int i = 0; i < 2; i++)
		{
			int roundIndex = 8 - i;
			if (isRoundOK(gi, team, roundIndex))
			{
				return Result.SUCCESSFUL;
			}
		}
		return Result.FAILED;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (announcing.isAnnounced(team, Announcements.nagyszincsaladok[getSuit()]))
			return false;
		
		return super.canBeAnnounced(announcing, team);
	}

	protected int getPoints()
	{
		return 60;
	}
}
