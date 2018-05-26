package com.tisza.tarock.announcement;

import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class PagatSasUltimo extends Ultimo
{
	PagatSasUltimo(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		Result zaroparosSuccessful = Announcements.zaroparos.isSuccessful(gameState, team);
		
		if (zaroparosSuccessful == Result.SUCCESSFUL)
		{
			return Result.FAILED;
		}
		
		return super.isSuccessful(gameState, team);
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		if (announcing.isAnnounced(team, Announcements.zaroparos))
			return false;
		
		if (getRoundIndex() == 8)
		{
			for (TarockCount tc : new TarockCount[]{Announcements.nyolctarokk, Announcements.kilenctarokk})
			{
				if (tc.canBeAnnounced(announcing))
				{
					return false;
				}
			}
		}
		
		return super.canBeAnnounced(announcing);
	}

	@Override
	public int getPoints()
	{
		return 10 * (9 - getRoundIndex());
	}

	@Override
	public boolean canBeSilent()
	{
		return getRoundIndex() == 8;
	}
}
