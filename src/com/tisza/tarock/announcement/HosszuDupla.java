package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class HosszuDupla extends Dupla
{
	public boolean canAnnounce(Map<Announcement, AnnouncementState> announcementStates, PlayerCards cards, int player, PlayerPairs pp)
	{
		if (!super.canAnnounce(announcementStates, cards, player, pp))
			return false;
		
		return cards.filter(new TarockFilter()).size() >= 7;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
