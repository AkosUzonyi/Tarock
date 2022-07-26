package com.tisza.tarock.server.player.bot.figure.paskievics;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.player.bot.*;
import com.tisza.tarock.server.player.bot.figure.*;

import java.util.*;

public class Game extends Figure
{
	public Game(Memory memory, Personality personality)
	{
		super(memory, personality);
	}

	@Override
	public Map<Card, Integer> fold(List<Card> skartableCards)
	{
		return null;
	}

	@Override
	public List<AnnouncementContra> announce(List<AnnouncementContra> enabledAnnouncements)
	{
		return null;
	}

	@Override
	public Map<Card, Integer> play(List<Card> availableCards)
	{
		/*		if (memory.getCurrentFirstCard() != null)
		{
			for (Card card : memory.getMyCards().getPlayableCards(memory.getCurrentFirstCard()).stream()
					.sorted(Comparator.comparingInt(this::getCardValue)).collect(Collectors.toList()))
			{
				if (card.doesBeat(memory.getCurrentStrongestCard()))
				{
					return card;
				}
			}
		}
		return cardToPlay;*/
		return null;
	}
}
