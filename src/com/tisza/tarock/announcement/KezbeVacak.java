package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class KezbeVacak extends AnnouncementBase
{
	private int roundIndex;
	private Card cardToTakeWith;
		
	KezbeVacak(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		Round round = gi.gameplay.getRoundsPassed().get(roundIndex);
		int theCardPlayer = round.getCards().indexOf(cardToTakeWith);
		if (theCardPlayer < 0) return Result.FAILED;
		
		if (gi.calling.getPlayerPairs().getTeam(theCardPlayer) != team)
			return Result.FAILED;
		
		if (round.getWinner() != theCardPlayer)
			return Result.FAILED;
		
		for (int i = 0; i < roundIndex; i++)
		{
			round = gi.gameplay.getRoundsPassed().get(i);
			int winner = round.getWinner();
			
			if (gi.calling.getPlayerPairs().getTeam(winner) != team)
				return Result.FAILED;
		}
		
		return Result.SUCCESSFUL;
	}

	public int getPoints()
	{
		return 10;
	}
}
