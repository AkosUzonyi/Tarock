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

	protected final boolean isRoundOK(GameState gameState, Team team, int roundIndex)
	{
		Round round = gameState.getRound(roundIndex);
		for (int p = 0; p < 4; p++)
		{
			Card card = round.getCardByPlayer(p);
			boolean isItUs = gameState.getPlayerPairs().getTeam(p) == team;
			boolean isCorrectSuit = card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
			boolean isWon = round.getWinner() == p;
			if (isItUs && isCorrectSuit && isWon)
			{
				return true;
			}
		}
		return false;
	}
	
	public final int getSuit()
	{
		return suit;
	}
	
	public boolean isShownInList()
	{
		return false;
	}
}
