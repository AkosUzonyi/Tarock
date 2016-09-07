package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class TakeRoundWithCard extends AnnouncementBase
{
	private int roundIndex;
	private Card cardToTakeWith;
		
	TakeRoundWithCard(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		Round r = gi.gameplay.getRoundsPassed().get(roundIndex);
		for (int theCardPlayer = 0; theCardPlayer < 4; theCardPlayer++)
		{
			Card card = r.getCards().get(theCardPlayer);
			if (!card.equals(cardToTakeWith)) continue;
			
			if (gi.calling.getPlayerPairs().getTeam(theCardPlayer) != team)
			{
				return Result.FAILED;
			}
			else
			{
				if (r.getWinner() == theCardPlayer)
				{
					return canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
				}
				else
				{
					return canBeSilent() ? Result.FAILED_SILENT : Result.FAILED;
				}
			}
		}
		return Result.FAILED;
	}

	public final int getRoundIndex()
	{
		return roundIndex;
	}

	public final Card getCardToTakeWith()
	{
		return cardToTakeWith;
	}

	public boolean canBeSilent()
	{
		return false;
	}
}
