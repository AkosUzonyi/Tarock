package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class TakeRoundWithCard extends AnnouncementBase
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
		if (canBeSilent())
		{
			int alma = 5;
			int u = alma;
		}
		
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

	public int getPoints(int winnerBid)
	{
		if (cardToTakeWith instanceof SuitCard && roundIndex == 8)
			return 15;
		
		if (cardToTakeWith.equals(new TarockCard(21)) && roundIndex == 8)
			return 21;
		
		int result = 10 * (9 - roundIndex);
		
		if (cardToTakeWith.equals(new TarockCard(21)))
			result += 10;
		
		return result;
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
		boolean is12 = cardToTakeWith.equals(new TarockCard(1)) || cardToTakeWith.equals(new TarockCard(2)); 
		return is12 && roundIndex == 8;
	}
}
