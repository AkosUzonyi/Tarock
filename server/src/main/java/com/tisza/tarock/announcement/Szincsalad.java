package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szincsalad extends LastRounds
{
	private final int suit;
	private final boolean kis;

	Szincsalad(int suit, boolean kis)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();

		this.suit = suit;
		this.kis = kis;
	}

	@Override
	public String getName()
	{
		return kis ? "kisszincsalad" : "nagyszincsalad";
	}

	@Override
	public final int getSuit()
	{
		return suit;
	}

	@Override
	public GameType getGameType()
	{
		return kis ? GameType.ZEBI : GameType.MAGAS;
	}

	@Override
	protected int getRoundCount()
	{
		return kis ? 2 : 3;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
	}

	@Override
	protected boolean isSameCategory(LastRounds otherAnnouncements)
	{
		return otherAnnouncements instanceof Szincsalad;
	}

	@Override
	protected int getPoints()
	{
		return kis ? 60 : 100;
	}
}
