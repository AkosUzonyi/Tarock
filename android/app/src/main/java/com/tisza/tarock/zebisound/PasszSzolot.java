package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class PasszSzolot extends ZebiSound
{
	private boolean solo;

	public PasszSzolot(Context context)
	{
		super(context, 1F, R.raw.passzolomszolot);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		solo = false;
	}

	@Override
	public void bid(int player, int bid)
	{
		if (bid == 0)
			solo = true;

		if (bid == -1 && solo)
			activate();
	}
}
