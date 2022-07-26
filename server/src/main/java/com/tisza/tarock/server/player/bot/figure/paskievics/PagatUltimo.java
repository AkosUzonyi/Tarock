package com.tisza.tarock.server.player.bot.figure.paskievics;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.player.bot.*;
import com.tisza.tarock.server.player.bot.figure.*;

import java.util.*;

public class PagatUltimo extends Figure
{
	public PagatUltimo(Memory memory, Personality personality)
	{
		super(memory, personality);
	}

	@Override
	public Map<Card, Integer> fold(List<Card> skartableCards)
	{
		Map<Card, Integer> result = new HashMap<>();
		skartableCards.forEach(card ->
		{
			if (card.equals(Card.getTarockCard(1)))
			{
				result.put(card, -6);
			}
			else if (card instanceof TarockCard)
			{
				result.put(card, -1);
			}
		});
		return result;
	}

	@Override
	public List<AnnouncementContra> announce(List<AnnouncementContra> enabledAnnouncements)
	{
		return null;
	}

	@Override
	public Map<Card, Integer> play(List<Card> availableCards)
	{
		Map<Card, Integer> result = new HashMap<>();
		availableCards.forEach(card ->
		{
			if (card.equals(Card.getTarockCard(1)))
			{
				if (memory.getRound() < 9)
				{
					result.put(card, -6);
				}
				else
				{
					result.put(card, 6);
				}
			}
		});
		return result;
	}
}
