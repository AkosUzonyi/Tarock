package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class LastRounds extends AnnouncementBase
{
	LastRounds(){}

	protected abstract int getRoundCount();
	protected abstract boolean isValidCard(Card card);

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (int i = 0; i < getRoundCount(); i++)
		{
			int roundIndex = 8 - i;
			if (!isRoundOK(gameState, team, roundIndex))
			{
				return Result.FAILED;
			}
		}
		return Result.SUCCESSFUL;
	}

	private boolean isRoundOK(GameState gameState, Team team, int roundIndex)
	{
		Round round = gameState.getRound(roundIndex);
		PlayerSeat winnerPlayer = round.getWinner();
		Card winnerCard = round.getCardByPlayer(winnerPlayer);

		boolean isItUs = gameState.getPlayerPairs().getTeam(winnerPlayer) == team;
		boolean isValidCard = isValidCard(winnerCard);

		return isItUs && isValidCard;
	}
}

