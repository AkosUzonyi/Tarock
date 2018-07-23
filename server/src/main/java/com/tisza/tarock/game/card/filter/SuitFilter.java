package com.tisza.tarock.game.card.filter;

import com.tisza.tarock.game.card.*;

public class SuitFilter implements CardFilter
{
	private final int suit;
	
	public SuitFilter()
	{
		suit = -1;
	}
	
	public SuitFilter(int s)
	{
		suit = s;
	}
	
	@Override
	public boolean match(Card c)
	{
		if (c instanceof SuitCard)
		{
			int s = ((SuitCard)c).getSuit();
			return suit < 0 || s == suit;
		}
		else
		{
			return false;
		}
	}

}
