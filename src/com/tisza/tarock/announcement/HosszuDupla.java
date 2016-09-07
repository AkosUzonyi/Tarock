package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;

public class HosszuDupla extends Dupla
{
	public boolean canBeAnnounced(Announcing announcing)
	{
		if (!super.canBeAnnounced(announcing))
			return false;
		
		PlayerCards cards = announcing.getCards().getPlayerCards(announcing.getNextPlayer());
		return cards.filter(new TarockFilter()).size() >= 7;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
