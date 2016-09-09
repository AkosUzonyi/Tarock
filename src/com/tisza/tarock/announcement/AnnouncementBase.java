package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(GameInstance gi, Team team);
	protected abstract int getPoints();
	
	public int calculatePoints(GameInstance gi, Team team)
	{
		Result r = isSuccessful(gi, team);
		
		PlayerPairs pp = gi.calling.getPlayerPairs();
		
		boolean isAnnounced = gi.announcing.isAnnounced(team, this);
		int contraMultiplier = (int)Math.pow(2, isAnnounced ? gi.announcing.getContraLevel(team, this) : 0);
		int winnerBid = pp.isSolo() && !pp.isSoloIntentional() ? 0 : gi.bidding.getWinnerBid();
		int winnerBidMultiplier = isMultipliedByWinnerBid() ? (4 - winnerBid) : 1;
		
		int points;
		if (isAnnounced)
		{
			if (r == Result.SUCCESSFUL || r == Result.SUCCESSFUL_SILENT)
			{
				points = getPoints();
			}
			else if (r == Result.FAILED || r == Result.FAILED_SILENT)
			{
				points = -getPoints();
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
				points = getPoints() / 2;
			}
			else if (r == Result.FAILED_SILENT)
			{
				points = -getPoints() / 2;
			}
			else
			{
				points = 0;
			}
		}
		
		points *= contraMultiplier;
		points *= winnerBidMultiplier;
		
		return points;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return false;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		return !announcing.isAnnounced(team, this);
	}
	
	public void onAnnounce(Announcing announcing, Team team)
	{
	}
	
	public boolean canContra()
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
