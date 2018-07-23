package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class HosszuDupla extends AnnouncementWrapper
{
	public HosszuDupla()
	{
		super(Announcements.dupla);
	}

	@Override
	public AnnouncementID getID()
	{
		return new AnnouncementID("hosszudupla");
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
