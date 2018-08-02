package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class Jobbfelni extends ZebiSound
{
	private boolean trullAnnounced;
	private int cardIndex;

	public Jobbfelni(Context context)
	{
		super(context);
	}

	@Override
	protected int getAudioRes()
	{
		return R.raw.jobbfelni;
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		trullAnnounced = false;
		cardIndex = 0;
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		if (announcement.getName().equals("trull"))
			trullAnnounced = true;
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		if (!trullAnnounced && cardIndex == 3 && card.equals(Card.getTarockCard(21)))
			activate();

		cardIndex++;
	}

	@Override
	public void cardsTaken(int winnerPlayer)
	{
		cardIndex = 0;
	}
}