package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class TakeRoundWithCard extends AnnouncementBase
{
	private int roundIndex;
	private Card cardToTakeWith;
	private boolean canBeSilent;
		
	TakeRoundWithCard(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
		
		boolean is12 = cardToTakeWith.equals(new TarockCard(1)) || cardToTakeWith.equals(new TarockCard(2)); 
		this.canBeSilent = is12 && roundIndex == 8;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		Round r = gi.gameplay.getRoundsPassed().get(roundIndex);
		for (int player = 0; player < 4; player++)
		{
			Card card = r.getCards().get(player);
			if (card != cardToTakeWith) continue;
			
			if (r.getWinner() == player && gi.calling.getPlayerPairs().getTeam(player) == team)
			{
				return canBeSilent ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
			}
			else
			{
				return canBeSilent ? Result.FAILED_SILENT : Result.FAILED;
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

	public int getRoundIndex()
	{
		return roundIndex;
	}

	public Card getCardToTakeWith()
	{
		return cardToTakeWith;
	}

	public boolean isCanBeSilent()
	{
		return canBeSilent;
	}
}
