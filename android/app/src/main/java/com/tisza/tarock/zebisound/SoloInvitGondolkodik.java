package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class SoloInvitGondolkodik extends ZebiSound
{
	private boolean twoNeedsToBeKept;

	public SoloInvitGondolkodik(Context context)
	{
		super(context, 1F, R.raw.szoloinvitgondolkozik);
	}

	@Override
	public void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		twoNeedsToBeKept = false;
	}

	@Override
	public void bid(int player, int bid)
	{
		cancelActivation();

		if (bid == 2)
			twoNeedsToBeKept = !twoNeedsToBeKept;

		if (bid < 2)
			twoNeedsToBeKept = false;
	}

	@Override
	public void availableBids(List<Integer> bids)
	{
		if (twoNeedsToBeKept && bids.contains(0))
			activateDelayed(2);
	}
}
