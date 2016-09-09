package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class TarockCount implements Announcement
{
	private int count;
	
	TarockCount(int count)
	{
		this.count = count;
	}
	
	public int calculatePoints(GameInstance gi, Team team)
	{
		return 0;
	}

	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (announcing.isAnnounced(team, this))
			return false;
		
		PlayerCards cards = announcing.getCards().getPlayerCards(announcing.getNextPlayer());
		return cards.filter(new TarockFilter()).size() == count;
	}

	public void onAnnounce(Announcing announcing, Team team)
	{
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
