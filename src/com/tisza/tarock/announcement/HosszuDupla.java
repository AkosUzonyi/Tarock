package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class HosszuDupla implements Announcement
{
	public int calculatePoints(GameInstance gi, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		PlayerCards cards = announcing.getCards().getPlayerCards(announcing.getNextPlayer());
		if (cards.filter(new TarockFilter()).size() < 7)
			return false;
		
		return Announcements.dupla.canBeAnnounced(announcing, team);
	}

	protected boolean canBeSilent()
	{
		return false;
	}

	public void onAnnounce(Announcing announcing, Team team)
	{
		announcing.announce(announcing.getNextPlayer(), Announcements.dupla);
	}

	public boolean canContra()
	{
		return false;
	}

	public boolean isShownToUser()
	{
		return true;
	}
}
