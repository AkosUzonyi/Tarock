package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Ezisamienk extends ZebiSound
{
	private int myID;
	private int takeCount;

	public Ezisamienk(Context context)
	{
		super(context, 1F, R.raw.nagyonszepezisamienk);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		this.myID = myID;
		takeCount = 0;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		if (winnerPlayer == myID)
		{
			takeCount++;
		}
		else
		{
			takeCount = 0;
		}

		if (takeCount >= 3)
			activate();
	}
}
