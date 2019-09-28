package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class TarokkKi extends ZebiSound
{
	private final int count;
	private int tarocks, suits;
	private boolean activated;

	public TarokkKi(Context context, int count, int audioRes)
	{
		super(context, 1F, audioRes);
		this.count = count;
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		tarocks = 0;
		suits = 0;
		activated = false;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (activated)
			return;

		if (playedCard instanceof TarockCard)
			tarocks++;
		else
			suits++;

		if (tarocks == count && suits < 6)
		{
			activate();
			activated = true;
		}
	}
}
