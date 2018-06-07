package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class TakeCards extends AnnouncementBase
{
	TakeCards(){}

	protected abstract boolean hasToBeTaken(Card card);
	protected abstract boolean canBeSilent();
	
	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (PlayerSeat opponentPlayer : gameState.getPlayerPairs().getPlayersInTeam(team.getOther()))
		{
			for (Card opponentCard : gameState.getWonCards(opponentPlayer))
			{
				if (hasToBeTaken(opponentCard))
				{
					return Result.FAILED;
				}
			}
		}

		return canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
	}
}
