package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class Szincsalad extends AnnouncementBase
{
	private int suit;
		
	Szincsalad(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
	}
	
	protected final boolean isRoundOK(GameInstance gi, Team team, int roundIndex)
	{
		Round round = gi.gameplay.getRoundsPassed().get(roundIndex);
		for (int p = 0; p < 4; p++)
		{
			Card card = round.getCards().get(p);
			boolean isItUs = gi.calling.getPlayerPairs().getTeam(p) == team;
			boolean isCorrectSuit = card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
			boolean isWon = round.getWinner() == p;
			if (isItUs && isCorrectSuit && isWon)
			{
				return true;
			}
		}
		return false;
	}
	
	protected final int getSuit()
	{
		return suit;
	}
	
	public boolean isShownToUser()
	{
		return false;
	}
}
