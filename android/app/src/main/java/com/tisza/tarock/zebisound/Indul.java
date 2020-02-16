package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

class Indul extends ZebiSound
{
	private boolean firstCard;

	public Indul(Context context)
	{
		super(context, 0.1F, R.raw.induloktisztan, R.raw.indulok, R.raw.tessek);
	}

	@Override
	public void startGame(GameType gameType, int beginnerPlayer)
	{
		firstCard = true;
	}

	@Override
	public void playCard(int player, Card card)
	{
		if (firstCard)
			activate();

		firstCard = false;
	}
}
