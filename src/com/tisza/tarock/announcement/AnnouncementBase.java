package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	public abstract Result isSuccessful(GameHistory gh, boolean callerTeam);
	public abstract int getPoints();
	
	public int calculatePoints(GameHistory gh, boolean callerTeam, boolean isSilent)
	{
		
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
