package com.tisza.tarock.card.filter;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class SkartableCardFilter implements CardFilter
{
	private final GameType gameType;

	public SkartableCardFilter(GameType gameType)
	{
		this.gameType = gameType;
	}

	@Override
	public boolean match(Card c)
	{
		if (c instanceof TarockCard)
		{
			int minValue = gameType.hasParent(GameType.MAGAS) ? 3 : 2;
			int value = ((TarockCard)c).getValue();
			return value >= minValue && value <= 20;
		}
		else
		{
			return ((SuitCard)c).getValue() != 5;
		}
	}
}
