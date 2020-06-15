package com.tisza.tarock.game.card.filter;

import com.tisza.tarock.game.card.*;
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
			int maxValue = gameType.hasParent(GameType.ZEBI) ? 20 : 19;
			int value = ((TarockCard)c).getValue();
			return value >= minValue && value <= maxValue;
		}
		else
		{
			return ((SuitCard)c).getValue() != 5;
		}
	}
}
