package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Skizultimo extends ZebiSound
{
	private int roundIndex;

	public Skizultimo(Context context)
	{
		super(context);
	}

	@Override
	protected int getAudioRes()
	{
		return R.raw.skizultimo;
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		roundIndex = 0;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		roundIndex++;
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		if (roundIndex == 8 && card.equals(Card.getTarockCard(22)))
			activate();
	}
}
