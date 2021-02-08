package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class Szinultimo extends Ultimo
{
	private final boolean isKing;
	
	Szinultimo(int trickIndex, SuitCard cardToTakeWith)
	{
		super(trickIndex, cardToTakeWith);
		isKing = cardToTakeWith.getValue() == 5;
	}

	@Override
	public GameType getGameType()
	{
		if (!isKing)
			return GameType.ZEBI;

		switch (getTrick())
		{
			case 8: case 7: return GameType.ILLUSZTRALT;
			case 6: case 5: return GameType.MAGAS;
		}

		throw new RuntimeException();
	}

	@Override
	public int getPoints()
	{
		if (getTrick() == 8)
		{
			return isKing ? 15 : 20;
		}
		else
		{
			return 10 * (9 - getTrick()) + (isKing ? 0 : 10);
		}
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		if (isKing && getTrick() == 8 && announcement == Announcements.kings[0])
			return true;

		return super.canOverrideAnnouncement(announcement);
	}
}
