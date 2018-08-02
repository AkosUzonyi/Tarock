package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Huszonegyfogasveszely extends ZebiSound
{
	private int nextExpectedBid;
	private int passzCount;
	private boolean deactivated;

	public Huszonegyfogasveszely(Context context)
	{
		super(context, 1F, R.raw.huszonegyfogasveszely);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		nextExpectedBid = 3;
		passzCount = 0;
		deactivated = false;
	}

	@Override
	public void bid(int player, int bid)
	{
		if (deactivated)
			return;

		if (bid < 0)
		{
			passzCount++;

			if (passzCount >= 2)
				deactivated = true;

			return;
		}

		if (nextExpectedBid != bid)
		{
			deactivated = true;
			return;
		}

		if (bid == 1)
			activate();

		nextExpectedBid--;
	}
}
