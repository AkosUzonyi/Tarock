package com.tisza.tarock.announcement;

import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.PlayerPairs;
import com.tisza.tarock.game.Team;

public abstract class AnnouncementBase implements Announcement
{
	protected abstract Result isSuccessful(GameState gameState, Team team);
	protected abstract int getPoints();
	
	public int calculatePoints(GameState gameState, Team team)
	{
		Result r = isSuccessful(gameState, team);
		
		PlayerPairs pp = gameState.getPlayerPairs();
		
		boolean isAnnounced = gameState.getAnnouncementsState().isAnnounced(team, this);
		int contraMultiplier = (int)Math.pow(2, isAnnounced ? gameState.getAnnouncementsState().getContraLevel(team, this) : 0);
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
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		return !announcing.isAnnounced(team, this);
	}
	
	public void onAnnounced(IAnnouncing announcing)
	{
	}
	
	public boolean canContra()
	{
		return true;
	}
	
	public boolean isShownInList()
	{
		return true;
	}
	
	public boolean requireIdentification()
	{
		return true;
	}
	
	public static enum Result
	{
		SUCCESSFUL, SUCCESSFUL_SILENT, FAILED, FAILED_SILENT, DEACTIVATED;
	}
}
