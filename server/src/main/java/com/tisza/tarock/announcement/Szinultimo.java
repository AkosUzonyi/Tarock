package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szinultimo extends Ultimo
{
	private final boolean isKing;
	
	Szinultimo(int roundIndex, SuitCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
		isKing = cardToTakeWith.getValue() == 5;
	}

	@Override
	public GameType getGameType()
	{
		if (!isKing)
			return GameType.ZEBI;

		switch (getRound())
		{
			case 8: case 7: return GameType.ILLUSZTRALT;
			case 6: case 5: return GameType.MAGAS;
		}

		throw new RuntimeException();
	}

	@Override
	public int getPoints()
	{
		if (getRound() == 8)
		{
			return isKing ? 15 : 20;
		}
		else
		{
			return 10 * (9 - getRound()) + (isKing ? 0 : 10);
		}
	}

	@Override
	protected boolean canOverrideAnnouncement(RoundAnnouncement announcement)
	{
		if (isKing && getRound() == 8 && announcement == Announcements.kings[0])
			return true;

		return super.canOverrideAnnouncement(announcement);
	}
}
