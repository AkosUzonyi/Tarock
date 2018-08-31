package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Plicit extends ZebiSound
{
	private int state;

	public Plicit(Context context)
	{
		super(context, 1F, R.raw.plicit, R.raw.szornyu);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		state = 0;
	}

	@Override
	public void bid(int player, int bid)
	{
		switch (state)
		{
			case 0:
				if (bid == 3)
					state++;
				break;
			case 1:
				if (bid == 2)
					state++;
				else
					state = -1;
				break;
			case 2:
				if (bid == 2)
					state++;
				else if (bid >= 0)
					state = -1;
				break;
			case 3:
				if (bid < 0)
					state++;
				else
					state = -1;
				break;
		}
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		if (phase == PhaseEnum.CHANGING && state == 4)
			activate();
	}
}
