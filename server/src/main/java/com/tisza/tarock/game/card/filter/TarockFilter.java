package com.tisza.tarock.game.card.filter;

import com.tisza.tarock.game.card.*;

public class TarockFilter implements CardFilter
{
	@Override
	public boolean match(Card c)
	{
		return c instanceof TarockCard;
	}
}
