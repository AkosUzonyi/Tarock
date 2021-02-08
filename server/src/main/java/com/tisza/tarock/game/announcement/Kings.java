package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class Kings extends LastTricks
{
	private static final int[] points = new int[]{8, 60, 100};
	
	private final int count;
		
	Kings(int count)
	{
		if (count < 1 || count >= 4)
			throw new IllegalArgumentException("invalid kings count: " + count);
		
		this.count = count;
	}

	@Override
	public String getID()
	{
		switch (count)
		{
			case 1: return "kiralyultimo";
			case 2: return "ketkiralyok";
			case 3: return "haromkiralyok";
		}
		throw new RuntimeException();
	}

	@Override
	public GameType getGameType()
	{
		switch (count)
		{
			case 1: case 2: return GameType.ZEBI;
			case 3:         return GameType.MAGAS;
		}
		throw new RuntimeException();
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		if (count == 1)
			return false;

		return super.canOverrideAnnouncement(announcement);
	}

	@Override
	protected int getTrickCount()
	{
		return count;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getValue() == 5;
	}

	@Override
	protected boolean isSameCategory(LastTricks otherAnnouncements)
	{
		return otherAnnouncements instanceof Kings;
	}

	@Override
	protected int getPoints()
	{
		return points[count - 1];
	}
}
