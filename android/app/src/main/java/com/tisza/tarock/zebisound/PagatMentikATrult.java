package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class PagatMentikATrult extends ZebiSound
{
	private boolean trullAnnounced;
	private boolean firstCardNext;

	public PagatMentikATrult(Context context)
	{
		super(context, 1F, R.raw.pagatmentikatrult);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		trullAnnounced = false;
		firstCardNext = true;
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getName().equals("trull"))
			trullAnnounced = true;
	}

	@Override
	public void playCard(int player, Card card)
	{
		if (trullAnnounced && firstCardNext && card instanceof TarockCard && ((TarockCard)card).getValue() == 1)
			activate();

		firstCardNext = false;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		firstCardNext = true;
	}
}
