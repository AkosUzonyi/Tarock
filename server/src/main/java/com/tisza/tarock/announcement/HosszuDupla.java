package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

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
		if (cards.getTarockCount() < 7)
			return false;
		
		return super.canBeAnnounced(announcing);
	}
}
