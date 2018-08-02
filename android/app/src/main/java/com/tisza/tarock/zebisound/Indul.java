package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Indul extends ZebiSound
{
	private boolean firstCard;

	public Indul(Context context)
	{
		super(context, 0.1F, R.raw.induloktisztan);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		firstCard = true;
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		if (firstCard)
			activate();

		firstCard = false;
	}
}
