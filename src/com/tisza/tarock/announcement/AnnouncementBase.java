package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	public abstract Result isSuccessful(GameInstance gi, Team team);
	public abstract int getPoints(int winnerBid);
	
	public int calculatePoints(GameInstance gi, Team team, boolean isAnnounced)
	{
		Result r = isSuccessful(gi, team);
		
		int winnerBid = gi.bidding.getWinnerBid();
		
		if (isAnnounced)
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
		else
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
