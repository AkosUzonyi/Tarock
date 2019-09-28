package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.card.*;

class CardSound extends ZebiSound
{
	private final Card card;

	public CardSound(Context context, Card card, int ... audioResources)
	{
		super(context, 0.2F, audioResources);
		this.card = card;
	}

	@Override
	public void playCard(int player, Card playedCard)
	{
		if (playedCard == card)
		{
			activate();
		}
	}
}
