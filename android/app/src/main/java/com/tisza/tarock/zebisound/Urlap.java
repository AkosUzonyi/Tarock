package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Urlap extends ZebiSound
{
	private boolean newRound;
	private boolean[] tarocksPlayed = new boolean[23];

	public Urlap(Context context)
	{
		super(context, 1F, R.raw.urlapomvan);
	}

	@Override
	public void startGame(List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		Arrays.fill(tarocksPlayed, false);
		newRound = true;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (playedCard instanceof TarockCard)
		{
			int tarockValue = ((TarockCard)playedCard).getValue();

			if (newRound && isUrlap(tarockValue))
				activate();

			tarocksPlayed[tarockValue] = true;
		}


		newRound = false;
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
		newRound = true;
	}
}
