package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(GameInstance gi, Team team);
	protected abstract int getPoints(int winnerBid);
	
	public int calculatePoints(GameInstance gi, Team team)
	{
		Result r = isSuccessful(gi, team);
		
		boolean isAnnounced = gi.announcing.isAnnounced(team, this);
		int contraMultiplier = (int)Math.pow(2, isAnnounced ? gi.announcing.getContraLevel(team, this) : 0);
		
		PlayerPairs pp = gi.calling.getPlayerPairs();
		int winnerBid = pp.isSolo() && !pp.isSoloIntentional() ? 0 : gi.bidding.getWinnerBid();
		
		int points;
		if (isAnnounced)
		{
			if (r == Result.SUCCESSFUL || r == Result.SUCCESSFUL_SILENT)
			{
				points = getPoints(winnerBid);
			}
			else if (r == Result.FAILED || r == Result.FAILED_SILENT)
			{
				points = -getPoints(winnerBid);
			}
			else
			{
				points = 0;
			}
		}
		else
		{
			if (r == Result.SUCCESSFUL_SILENT)
			{
				points = getPoints(winnerBid) / 2;
			}
			else if (r == Result.FAILED_SILENT)
			{
				points = -getPoints(winnerBid) / 2;
			}
			else
			{
				points = 0;
			}
		}
		
		points *= contraMultiplier;
		
		return points;
	}
	
	public final int getID()
	{
		return Announcements.getID(this);
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		return true;
	}
	
	public boolean isShownToUser()
	{
		return true;
	}
	
	public static enum Result
	{
		SUCCESSFUL, SUCCESSFUL_SILENT, FAILED, FAILED_SILENT, DEACTIVATED;
	}
}
