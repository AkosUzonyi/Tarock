package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

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
	public GameType getGameType()
	{
		return GameType.MAGAS;
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
