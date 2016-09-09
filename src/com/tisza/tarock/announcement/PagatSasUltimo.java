package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class PagatSasUltimo extends Ultimo
{
	PagatSasUltimo(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}
	
	public Result isSuccessful(GameInstance gi, Team team)
	{
		Result zaroparosSuccessful = Announcements.zaroparos.isSuccessful(gi, team);
		
		if (zaroparosSuccessful == Result.SUCCESSFUL)
		{
			return Result.FAILED;
		}
		
		return super.isSuccessful(gi, team);
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (announcing.isAnnounced(team, Announcements.zaroparos))
			return false;
		
		if (getRoundIndex() == 8)
		{
			for (TarockCount tc : new TarockCount[]{Announcements.nyolctarokk, Announcements.kilenctarokk})
			{
				if (tc.canBeAnnounced(announcing, team) && !announcing.isAnnounced(team, tc))
				{
					return false;
				}
			}
		}
		
		return super.canBeAnnounced(announcing, team);
	}

	public int getPoints()
	{
		return 10 * (9 - getRoundIndex());
	}

	public boolean canBeSilent()
	{
		return getRoundIndex() == 8;
	}
}
