package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(GameState gameState, Team team);
	protected abstract int getPoints();
	
	@Override
	public int calculatePoints(GameState gameState, Team team)
	{
		Result r = isSuccessful(gameState, team);
		
		PlayerPairs pp = gameState.getPlayerPairs();
		
		boolean isAnnounced = gameState.getAnnouncementsState().isAnnounced(team, this);
		int contraLevel = isAnnounced ? gameState.getAnnouncementsState().getContraLevel(team, this) : 0;
		int winnerBid = pp.isSolo() && !gameState.isSoloIntentional() ? 0 : gameState.getWinnerBid();
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
		Team team = announcing.getCurrentTeam();
		
		return !announcing.isAnnounced(team, this);
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
	public boolean isShownInList()
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

	public static enum Result
	{
		SUCCESSFUL, SUCCESSFUL_SILENT, FAILED, FAILED_SILENT, DEACTIVATED;
	}
}
