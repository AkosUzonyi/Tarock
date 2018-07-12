package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Kemenyenbelecsap extends ZebiSound
{
	private Card firstCard;

	public Kemenyenbelecsap(Context context)
	{
		super(context, 0.4F, R.raw.kemenyenbelecsapni);
	}

	@Override
	public void startGame(List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		firstCard = null;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (firstCard == null)
			firstCard = playedCard;

		if (firstCard instanceof SuitCard && playedCard.equals(Card.getTarockCard(22)))
			activate();
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		firstCard = null;
	}
}
