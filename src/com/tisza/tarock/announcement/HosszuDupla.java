package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class HosszuDupla extends Dupla
{
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		PlayerCards cards = announcing.getCards().getPlayerCards(announcing.getNextPlayer());
		if (cards.filter(new TarockFilter()).size() < 7)
			return false;
		
		return super.canBeAnnounced(announcing, team);
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
