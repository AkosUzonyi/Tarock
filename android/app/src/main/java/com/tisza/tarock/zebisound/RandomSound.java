package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.game.*;

import java.util.*;

class RandomSound extends ZebiSound
{
	public RandomSound(Context context, int ... audioResources)
	{
		super(context, 1F, audioResources);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		cancelActivation();
		activateDelayed(rnd.nextInt(180));
	}
}
