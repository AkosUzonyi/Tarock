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

	public String getName()
	{
		return "nagyszincsalad";
	}

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
	
	public void onAnnounce(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		announcing.clearAnnouncement(team, Announcements.kisszincsaladok[getSuit()]);
	}

	protected int getPoints()
	{
		return 100;
	}
}
