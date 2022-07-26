package com.tisza.tarock.server.player.bot.figure.paskievics;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.player.bot.*;
import com.tisza.tarock.server.player.bot.figure.*;

import java.util.*;

public class EightTarock extends Figure
{
	public EightTarock(Memory memory, Personality personality)
	{
		super(memory, personality);
	}

	@Override
	public Map<Card, Integer> fold(List<Card> skartableCards)
	{
		Map<Card, Integer> result = new HashMap<>();
		skartableCards.forEach(card ->
		{
			if (card instanceof TarockCard)
			{
				result.put(card, -1);
			}
		});
		return result;
	}

	@Override
	public List<AnnouncementContra> announce(List<AnnouncementContra> enabledAnnouncements)
	{
		if (memory.getMyCards().getTarockCount() == 8)
		{
			return Collections.singletonList(new AnnouncementContra(Announcements.nyolctarokk, -1));
		}
		return Collections.emptyList();
	}
}
