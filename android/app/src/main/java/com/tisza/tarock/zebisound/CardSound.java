package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.card.*;

class CardSound extends ZebiSound
{
	private final Card card;
	private final int[] audioResources;

	public CardSound(Context context, Card card, int ... audioResources)
	{
		super(context);
		this.card = card;
		this.audioResources = audioResources;
	}

	@Override
	protected int getAudioRes()
	{
		return audioResources[rnd.nextInt(audioResources.length)];
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
	}

	@Override
	public void cardPlayed(int player, Card playedCard)
	{
		if (playedCard == card)
		{
			activate();
		}
	}
}
