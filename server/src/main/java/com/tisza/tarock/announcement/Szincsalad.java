package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public abstract class Szincsalad extends AnnouncementBase
{
	private int suit;
		
	Szincsalad(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
	}

	protected abstract int getSize();

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (int i = 0; i < getSize(); i++)
		{
			int roundIndex = 8 - i;
			if (!isRoundOK(gameState, team, roundIndex))
			{
				return Result.FAILED;
			}
		}
		return Result.SUCCESSFUL;
	}

	protected final boolean isRoundOK(GameState gameState, Team team, int roundIndex)
	{
		Round round = gameState.getRound(roundIndex);
		int winnerPlayer = round.getWinner();
		Card winnerCard = round.getCardByPlayer(winnerPlayer);

		boolean isItUs = gameState.getPlayerPairs().getTeam(winnerPlayer) == team;
		boolean isCorrectSuit = winnerCard instanceof SuitCard && ((SuitCard)winnerCard).getSuit() == suit;

		return isItUs && isCorrectSuit;
	}
	
	@Override
	public final int getSuit()
	{
		return suit;
	}
	
	@Override
	public boolean isShownInList()
	{
		return false;
	}
}
