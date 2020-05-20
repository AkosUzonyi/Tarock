package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public class Zaroparos extends LastRounds
{
	Zaroparos(){}

	@Override
	public String getID()
	{
		return "zaroparos";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.MAGAS;
	}

	@Override
	protected int getRoundCount()
	{
		return 2;
	}

	@Override
	protected boolean isValidCard(Card card)
	{
		if (!(card instanceof TarockCard))
			return false;

		TarockCard tarockCard = (TarockCard)card;
		return tarockCard.getValue() == 1 || tarockCard.getValue() == 2;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();

		for (int round = 0; round < 7; round++)
		{
			for (int tarock = 1; tarock <= 2; tarock++)
			{
				Ultimo pagatsasUltimo = Announcements.ultimok.get(Card.getTarockCard(tarock)).get(round);

				if (announcing.isAnnounced(team, pagatsasUltimo))
					return false;
			}
		}

		return super.canBeAnnounced(announcing);
	}

	@Override
	protected boolean isSameCategory(LastRounds otherAnnouncements)
	{
		return otherAnnouncements instanceof Zaroparos;
	}

	@Override
	protected int getPoints()
	{
		return 40;
	}
}
