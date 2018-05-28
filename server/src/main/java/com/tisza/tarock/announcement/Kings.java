package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Kings extends LastRounds
{
	private static final int[] points = new int[]{8, 60, 100};
	
	private int count;
		
	Kings(int count)
	{
		if (count < 1 || count >= 4)
			throw new IllegalArgumentException();
		
		this.count = count;
	}

	@Override
	public String getName()
	{
		switch (count)
		{
			case 1:
				return "kiralyultimo";
			case 2:
				return "ketkiralyok";
			case 3:
				return "haromkiralyok";
			default:
				throw new Error();
		}
	}

	@Override
	protected int getRoundCount()
	{
		return count;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		return card instanceof SuitCard && ((SuitCard)card).getValue() == 5;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int i = 3; i >= count; i--)
		{
			if (announcing.isAnnounced(team, Announcements.kings[i - 1]))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing);
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int i = 1; i < count; i++)
		{
			announcing.clearAnnouncement(team, Announcements.kings[i - 1]);
		}
	}

	@Override
	protected int getPoints()
	{
		return points[count - 1];
	}
}
