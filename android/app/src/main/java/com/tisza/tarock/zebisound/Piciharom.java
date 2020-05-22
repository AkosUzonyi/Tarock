package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Piciharom extends ZebiSound
{
	public Piciharom(Context context)
	{
		super(context, 0.15F, R.raw.piciharom);
	}

	@Override
	public void bid(int player, int bid)
	{
		if (bid == 3)
			activate();
	}
}
