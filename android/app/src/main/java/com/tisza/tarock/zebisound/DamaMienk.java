package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class DamaMienk extends ZebiSound
{
	private int myID;
	private Card[] lastCards = new Card[4];

	public DamaMienk(Context context)
	{
		super(context, 1F, R.raw.ezazdamamienk);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		Arrays.fill(lastCards, null);
	}

	@Override
	public void cardPlayed(int player, Card playedCard)
	{
		lastCards[player] = playedCard;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		Card winnerCard = lastCards[winnerPlayer];
		if (winnerCard instanceof SuitCard && ((SuitCard)winnerCard).getValue() == 4)
			activate();
	}
}
