package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class HosszuDupla extends AnnouncementBridge
{
	public HosszuDupla()
	{
		super(Announcements.dupla);
	}

	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		if (cards.filter(new TarockFilter()).size() < 7)
			return false;
		
		return super.canBeAnnounced(announcing);
	}
}
