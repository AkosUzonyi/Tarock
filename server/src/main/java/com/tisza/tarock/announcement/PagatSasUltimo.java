package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class PagatSasUltimo extends Ultimo
{
	PagatSasUltimo(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}

	@Override
	public GameType getGameType()
	{
		if (getCard().equals(Card.getTarockCard(2)))
			return GameType.MAGAS;

		switch (getRound())
		{
			case 8:         return GameType.PASKIEVICS;
			case 7:         return GameType.ILLUSZTRALT;
			case 6: case 5: return GameType.MAGAS;
		}
		throw new RuntimeException();
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
		
		if (getRound() == 8)
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
		return 10 * (9 - getRound());
	}

	@Override
	public boolean canBeSilent()
	{
		return getRound() == 8;
	}
}
