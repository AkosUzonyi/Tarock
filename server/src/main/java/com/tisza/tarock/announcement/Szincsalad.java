package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

public abstract class Szincsalad extends LastRounds
{
	private final int suit;
		
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
}
