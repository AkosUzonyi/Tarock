package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class KezbeVacak extends TakeRoundWithCard
{
	KezbeVacak(int roundIndex, Card cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		if (super.isSuccessful(gi, team) == Result.FAILED)
			return Result.FAILED;
		
		for (int i = 0; i < getRoundIndex(); i++)
		{
			Round round = gi.gameplay.getRoundsPassed().get(i);
			int winner = round.getWinner();
			if (gi.calling.getPlayerPairs().getTeam(winner) != team)
				return Result.FAILED;
		}
		
		return Result.SUCCESSFUL;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		return true;
	}
	
	public Announcement getAnnouncementOverridden(Announcing announcing, Team team)
	{
		return null;
	}
	
	public final boolean canBeSilent()
	{
		return false;
	}

	public int getPoints()
	{
		return 10;
	}
	
	public boolean isShownToUser()
	{
		return true;
	}
}
