package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

class Licit1 extends ZebiSound
{
	private int lastBid;

	public Licit1(Context context)
	{
		super(context, 0.4F, R.raw.szuperlicit, R.raw.hemondomegy);
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
