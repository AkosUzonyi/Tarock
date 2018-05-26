package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.game.*;
import com.tisza.tarock.player.*;

public class KezbeVacak extends AnnouncementBase
{
	private int roundIndex;
	private Card cardToTakeWith;
		
	KezbeVacak(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	@Override
	public String getName()
	{
		return "kezbevacak";
	}

	@Override
	public int getRound()
	{
		return roundIndex;
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(roundIndex);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer == null) return Result.FAILED;
		
		if (gameState.getPlayerPairs().getTeam(theCardPlayer) != team)
			return Result.FAILED;
		
		if (round.getWinner() != theCardPlayer)
			return Result.FAILED;
		
		for (int i = 0; i < roundIndex; i++)
		{
			round = gameState.getRound(i);
			PlayerSeat winner = round.getWinner();
			
			if (gameState.getPlayerPairs().getTeam(winner) != team)
				return Result.FAILED;
		}
		
		return Result.SUCCESSFUL;
	}

	@Override
	public int getPoints()
	{
		return 10;
	}
}
