package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

class Ezisamienk extends ZebiSound
{
	private int lastWinnerPlayer;
	private int takeCount;

	public Ezisamienk(Context context)
	{
		super(context, 1F, R.raw.nagyonszepezisamienk);
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		lastWinnerPlayer = -1;
		takeCount = 0;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		if (winnerPlayer != lastWinnerPlayer)
		{
			lastWinnerPlayer = winnerPlayer;
			takeCount = 0;
		}

		takeCount++;
		if (takeCount >= 3)
			activate();
	}
}
