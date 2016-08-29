package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(Gameplay gp, PlayerPairs pp, Team team);
	protected abstract int getPoints(int winnerBid);
	
	public int calculatePoints(Gameplay gp, PlayerPairs pp, Team team, int winnerBid, boolean isSilent)
	{
		Result r = isSuccessful(gp, pp, team);
		
		if (isSilent)
		{
			if (r == Result.SUCCESSFUL_SILENT)
			{
				return getPoints(winnerBid) / 2;
			}
			else if (r == Result.FAILED_SILENT)
			{
				return -getPoints(winnerBid) / 2;
			}
			else
			{
				return 0;
			}
		}
		else
		{
			if (r == Result.SUCCESSFUL || r == Result.SUCCESSFUL_SILENT)
			{
				return getPoints(winnerBid);
			}
			else if (r == Result.FAILED || r == Result.FAILED_SILENT)
			{
				return -getPoints(winnerBid);
			}
			else
			{
				return 0;
			}
		}
	}	
	
	public String getName()
	{
		throw new UnsupportedOperationException();
	}
	
	public static enum Result
	{
		SUCCESSFUL, SUCCESSFUL_SILENT, FAILED, FAILED_SILENT, DEACTIVATED;
	}
}
