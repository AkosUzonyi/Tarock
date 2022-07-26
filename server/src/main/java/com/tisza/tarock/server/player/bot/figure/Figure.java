package com.tisza.tarock.server.player.bot.figure;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.player.bot.*;

import java.util.*;

public abstract class Figure
{
	protected final Memory memory;
	protected final Personality personality;

	public Figure(Memory memory, Personality personality)
	{
		this.memory = memory;
		this.personality = personality;
	}

	public Map<Card, Integer> fold(List<Card> skartableCards)
	{
		return Collections.emptyMap();
	}

	public List<AnnouncementContra> announce(List<AnnouncementContra> enabledAnnouncements)
	{
		return Collections.emptyList();
	}

	public Map<Card, Integer> play(List<Card> availableCards)
	{
		return Collections.emptyMap();
	}
}
