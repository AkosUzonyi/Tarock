package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Kings extends AnnouncementBase
{
	private static final int[] points = new int[]{8, 60, 100};
	
	private int count;
		
	Kings(int count)
	{
		if (count < 1 || count >= 4)
			throw new IllegalArgumentException();
		
		this.count = count;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		for (int i = 0; i < count; i++)
		{
			int roundIndex = 8 - i;
			if (isRoundOK(gi, team, roundIndex))
			{
				return Result.SUCCESSFUL;
			}
		}
		return Result.FAILED;
	}
	
	private boolean isRoundOK(GameInstance gi, Team team, int roundIndex)
	{
		Round round = gi.gameplay.getRoundsPassed().get(roundIndex);
		for (int p = 0; p < 4; p++)
		{
			Card card = round.getCards().get(p);
			boolean isItUs = gi.calling.getPlayerPairs().getTeam(p) == team;
			boolean isKing = card instanceof SuitCard && ((SuitCard)card).getValue() == 5;
			boolean isWon = round.getWinner() == p;
			if (isItUs && isKing && isWon)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		for (int i = 3; i >= count; i--)
		{
			if (announcing.isAnnounced(team, Announcements.kings[i - 1]))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing, team);
	}
	
	public void onAnnounce(Announcing announcing, Team team)
	{
		for (int i = 1; i < count; i++)
		{
			announcing.clearAnnouncement(team, Announcements.kings[i - 1]);
		}
	}

	protected int getPoints()
	{
		return points[count - 1];
	}
}
