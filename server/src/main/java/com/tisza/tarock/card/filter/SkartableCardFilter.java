package com.tisza.tarock.card.filter;

import com.tisza.tarock.card.*;

public class SkartableCardFilter implements CardFilter
{
	@Override
	public boolean match(Card c)
	{
		if (c instanceof TarockCard)
		{
			int value = ((TarockCard)c).getValue();
			return value >= 3 && value <= 20;
		}
		else
		{
			return ((SuitCard)c).getValue() != 5;
		}
	}
}
