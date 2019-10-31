package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(Game game, Team team);
	protected abstract int getPoints();
	
	@Override
	public int calculatePoints(Game game, Team team)
	{
		Result r = isSuccessful(game, team);
		
		PlayerPairs pp = game.getPlayerPairs();
		
		boolean isAnnounced = game.getAnnouncementsState().isAnnounced(team, this);
		int contraLevel = isAnnounced ? game.getAnnouncementsState().getContraLevel(team, this) : 0;
		int winnerBid = pp.isSolo() && !game.isSoloIntentional() ? 0 : game.getWinnerBid();
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
				points = getSilentPoints();
			}
			else if (r == Result.FAILED_SILENT)
			{
				points = -getSilentPoints();
			}
			else
			{
				points = 0;
			}
		}
		
		points <<= contraLevel;
		points *= winnerBidMultiplier;
		
		return points;
	}

	protected int getSilentPoints()
	{
		return getPoints() / 2;
	}
	
	protected boolean isMultipliedByWinnerBid()
	{
		return false;
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return true;
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
	}
	
	@Override
	public boolean canContra()
	{
		return true;
	}
	
	@Override
	public boolean requireIdentification()
	{
		return true;
	}

	@Override
	public boolean shouldBeStored()
	{
		return true;
	}

	public enum Result
	{
		SUCCESSFUL, SUCCESSFUL_SILENT, FAILED, FAILED_SILENT, DEACTIVATED
	}
}
