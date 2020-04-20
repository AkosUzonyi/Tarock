package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

class TarokkKi extends ZebiSound
{
	private final int count;
	private boolean ultimo;
	private int tarocks, suits;
	private boolean activated;

	public TarokkKi(Context context, int count, int audioRes)
	{
		super(context, 1F, audioRes);
		this.count = count;
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		ultimo = false;
		tarocks = 0;
		suits = 0;
		activated = false;
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getID().substring(1).startsWith("ultimo"))
			ultimo = true;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (!ultimo || activated)
			return;

		if (playedCard instanceof TarockCard)
			tarocks++;
		else
			suits++;

		if (tarocks == count && suits < 4)
		{
			activate();
			activated = true;
		}
	}
}
