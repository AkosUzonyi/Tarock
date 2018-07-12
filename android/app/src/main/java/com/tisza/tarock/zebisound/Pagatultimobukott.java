package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Pagatultimobukott extends ZebiSound
{
	private int round;
	private int pagatInLastRoundPlayer;

	public Pagatultimobukott(Context context)
	{
		super(context, 1F, R.raw.tessekminuszot);
	}

	@Override
	public void startGame(List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		round = 0;
		pagatInLastRoundPlayer = -1;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (round == 8 && playedCard.equals(Card.getTarockCard(1)))
			pagatInLastRoundPlayer = player;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		round++;

		if (pagatInLastRoundPlayer >= 0 && pagatInLastRoundPlayer != winnerPlayer)
			activate();
	}
}
