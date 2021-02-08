package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Urlap extends ZebiSound
{
	private boolean newTrick;
	private boolean[] tarocksPlayed = new boolean[23];

	public Urlap(Context context)
	{
		super(context, 0.4F, R.raw.urlapomvan);
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		Arrays.fill(tarocksPlayed, false);
		newTrick = true;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (playedCard instanceof TarockCard)
		{
			int tarockValue = ((TarockCard)playedCard).getValue();

			if (newTrick && isUrlap(tarockValue))
				activate();

			tarocksPlayed[tarockValue] = true;
		}


		newTrick = false;
	}

	private boolean isUrlap(int tarockValue)
	{
		for (int i = 22; i > tarockValue; i--)
			if (!tarocksPlayed[i])
				return false;

		return true;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		newTrick = true;
	}
}
