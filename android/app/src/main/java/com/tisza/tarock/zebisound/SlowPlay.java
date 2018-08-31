package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.card.*;

class SlowPlay extends ZebiSound
{
	public SlowPlay(Context context)
	{
		super(context, 0.3F, R.raw.nategyedmar, R.raw.negondoljatoktul);
	}

	@Override
	public void cardPlayed(int player, Card playedCard)
	{
		cancelActivation();
		activateDelayed(5000);
	}
}
