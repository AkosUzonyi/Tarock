package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

class Szuperlicit extends ZebiSound
{
	private int lastBid;

	public Szuperlicit(Context context)
	{
		super(context, 0.3F, R.raw.szuperlicit);
	}

	@Override
	public void bid(int player, int bid)
	{
		if (bid < 0)
			return;

		lastBid = bid;
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		if (phase == PhaseEnum.CHANGING && lastBid == 1)
			activate();
	}
}
