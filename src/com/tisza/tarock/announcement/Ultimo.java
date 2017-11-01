package com.tisza.tarock.announcement;

import java.util.Map;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public abstract class Ultimo extends AnnouncementBase
{
	private int roundIndex;
	private Card cardToTakeWith;
		
	Ultimo(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	public Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(roundIndex);
		int theCardPlayer = round.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer < 0) return Result.FAILED;
		
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
	
	public void onAnnounce(IAnnouncing announcing)
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

	protected final int getRoundIndex()
	{
		return roundIndex;
	}

	protected final Card getCardToTakeWith()
	{
		return cardToTakeWith;
	}

	public boolean canBeSilent()
	{
		return false;
	}
	
	public boolean isShownToUser()
	{
		return false;
	}
}
