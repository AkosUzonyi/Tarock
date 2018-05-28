package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public abstract class Szincsalad extends LastRounds
{
	private int suit;
		
	Szincsalad(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
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
