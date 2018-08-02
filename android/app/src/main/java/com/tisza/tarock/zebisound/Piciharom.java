package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Piciharom extends ZebiSound
{
	public Piciharom(Context context)
	{
		super(context);
	}

	@Override
	protected int getAudioRes()
	{
		return R.raw.piciharom;
	}

	@Override
	protected float getFrequency()
	{
		return 1F;
	}

	@Override
	public void bid(int player, int bid)
	{
		if (bid == 3)
			activate();
	}
}
