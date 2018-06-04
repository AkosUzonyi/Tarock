package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public abstract class Ultimo extends AnnouncementBase
{
	private final int roundIndex;
	private final Card cardToTakeWith;
		
	Ultimo(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	@Override
	public String getName()
	{
		return "ultimo";
	}

	@Override
	public Card getCard()
	{
		return cardToTakeWith;
	}

	@Override
	public int getRound()
	{
		return roundIndex;
	}

	@Override
	protected Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(roundIndex);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer == null) return Result.FAILED;
		
		if (gameState.getPlayerPairs().getTeam(theCardPlayer) != team)
		{
			return Result.FAILED;
		}
		else
		{
			if (round.getWinner() == theCardPlayer)
			{
				return canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
			}
			else
			{
				return canBeSilent() ? Result.FAILED_SILENT : Result.FAILED;
			}
		}
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		Map<Integer, Ultimo> ultimokFromMyCard = Announcements.ultimok.get(cardToTakeWith);
		
		for (int r = 0; r <= roundIndex; r++)
		{
			Ultimo announcement = ultimokFromMyCard.get(r);
			if (announcement != null && announcing.isAnnounced(team, announcement))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing);
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		Map<Integer, Ultimo> ultimokFromMyCard = Announcements.ultimok.get(cardToTakeWith);
		
		for (int r = 8; r > roundIndex; r--)
		{
			Ultimo announcement = ultimokFromMyCard.get(r);
			if (announcement != null)
			{
				announcing.clearAnnouncement(team, announcement);
			}
		}
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
