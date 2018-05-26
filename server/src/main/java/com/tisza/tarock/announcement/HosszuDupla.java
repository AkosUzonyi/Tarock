package com.tisza.tarock.announcement;

import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.filter.TarockFilter;

public class HosszuDupla extends AnnouncementWrapper
{
	public HosszuDupla()
	{
		super(Announcements.dupla);
	}

	@Override
	public String getName()
	{
		return "hosszudupla";
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		PlayerCards cards = announcing.getCards(announcing.getCurrentPlayer());
		if (cards.filter(new TarockFilter()).size() < 7)
			return false;
		
		return super.canBeAnnounced(announcing);
	}
}
