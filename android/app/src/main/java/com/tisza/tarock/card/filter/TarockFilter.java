package com.tisza.tarock.card.filter;

import com.tisza.tarock.card.*;

public class TarockFilter implements CardFilter
{
	public boolean match(Card c)
	{
		return c instanceof TarockCard;
	}
}
