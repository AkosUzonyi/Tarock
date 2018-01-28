package com.tisza.tarock.card.filter;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.TarockCard;

public class TarockFilter implements CardFilter
{
	public boolean match(Card c)
	{
		return c instanceof TarockCard;
	}
}
