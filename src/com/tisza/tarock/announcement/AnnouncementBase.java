package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	public abstract Result isSuccessful(GameHistory gh, boolean callerTeam);
	public abstract int getPoints();
	
	public int calculatePoints(GameHistory gh, boolean callerTeam, boolean isSilent)
	{
		Result r = isSuccessful(gh, callerTeam);
		
		if (isSilent)
		{
			if (r == Result.SUCCESSFUL_SILENT)
			{
				return getPoints() / 2;
			}
			else if (r == Result.FAILED_SILENT)
			{
				return -getPoints() / 2;
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
				return getPoints();
			}
			else if (r == Result.FAILED || r == Result.FAILED_SILENT)
			{
				return -getPoints();
			}
			else
			{
				return 0;
			}
		}
	}	
	
	public boolean isSilent()
	{
		return false;
	}
	
	public boolean hasSilentPair()
	{
		return false;
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
