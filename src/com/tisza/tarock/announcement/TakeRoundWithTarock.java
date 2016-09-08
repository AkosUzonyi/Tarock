package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class TakeRoundWithTarock extends TakeRoundWithCard
{
	private static final List<Card> cardsRequiresTarockCount = new ArrayList<Card>();
	
	TakeRoundWithTarock(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (cardsRequiresTarockCount.contains(getCardToTakeWith()) && getRoundIndex() == 8)
		{
			for (TarockCount tc : new TarockCount[]{Announcements.nyolctarokk, Announcements.kilenctarokk})
			{
				if (tc.canBeAnnounced(announcing, team) && !announcing.isAnnounced(team, tc))
				{
					return false;
				}
			}
		}
		
		return super.canBeAnnounced(announcing, team);
	}

	public int getPoints()
	{
		return 10 * (9 - getRoundIndex());
	}

	public boolean canBeSilent()
	{
		return getRoundIndex() == 8;
	}
	
	static
	{
		cardsRequiresTarockCount.add(new TarockCard(1));
		cardsRequiresTarockCount.add(new TarockCard(2));
	}
}
