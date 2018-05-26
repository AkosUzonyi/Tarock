package com.tisza.tarock.announcement;

import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Team;

public class Nagyszincsalad extends Szincsalad
{
	Nagyszincsalad(int suit)
	{
		super(suit);
	}

	@Override
	public String getName()
	{
		return "nagyszincsalad";
	}

	@Override
	protected Result isSuccessful(GameState gameState, Team team)
	{
		for (int i = 0; i < 3; i++)
		{
			int roundIndex = 8 - i;
			if (isRoundOK(gameState, team, roundIndex))
			{
				return Result.SUCCESSFUL;
			}
		}
		return Result.FAILED;
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		announcing.clearAnnouncement(team, Announcements.kisszincsaladok[getSuit()]);
	}

	@Override
	protected int getPoints()
	{
		return 100;
	}
}
