package com.tisza.tarock.announcement;

import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.filter.TarockFilter;
import com.tisza.tarock.game.IAnnouncing;

public class HosszuDupla extends AnnouncementWrapper
{
	public HosszuDupla()
	{
		super(Announcements.dupla);
	}

	public String getName()
	{
		return "hosszudupla";
	}

	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		if (cards.filter(new TarockFilter()).size() < 7)
			return false;
		
		return super.canBeAnnounced(announcing);
	}
}
