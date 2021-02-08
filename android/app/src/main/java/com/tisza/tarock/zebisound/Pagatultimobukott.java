package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

class Pagatultimobukott extends ZebiSound
{
	private int trick;
	private int pagatInLastTrickPlayer;

	public Pagatultimobukott(Context context)
	{
		super(context, 1F, R.raw.tessekminuszot);
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		trick = 0;
		pagatInLastTrickPlayer = -1;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (trick == 8 && playedCard.equals(Card.getTarockCard(1)))
			pagatInLastTrickPlayer = player;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		trick++;

		if (pagatInLastTrickPlayer >= 0 && pagatInLastTrickPlayer != winnerPlayer)
			activate();
	}
}
