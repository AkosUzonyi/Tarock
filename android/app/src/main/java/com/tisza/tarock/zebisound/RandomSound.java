package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

class RandomSound extends ZebiSound
{
	public RandomSound(Context context, int ... audioResources)
	{
		super(context, 0.01F, audioResources);
	}

	@Override
	public void cardPlayed(int player, Card card)
	{
		activate();
	}
}
