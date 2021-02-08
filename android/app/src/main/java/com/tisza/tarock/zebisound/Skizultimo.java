package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

class Skizultimo extends ZebiSound
{
	private int trickIndex;

	public Skizultimo(Context context)
	{
		super(context, 1F, R.raw.skizultimo);
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		trickIndex = 0;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		trickIndex++;
	}

	@Override
	public void playCard(int player, Card card)
	{
		if (trickIndex == 8 && card.equals(Card.getTarockCard(22)))
			activate();
	}
}
