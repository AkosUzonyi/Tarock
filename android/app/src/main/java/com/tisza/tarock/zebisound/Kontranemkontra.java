package com.tisza.tarock.zebisound;

import android.content.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;

import java.util.*;

class Kontranemkontra extends ZebiSound
{
	private boolean trullAnnounced;
	private int cardIndex;

	public Kontranemkontra(Context context)
	{
		super(context, 1F, R.raw.kontranemkontra, R.raw.gondolkozikkontra);
	}

	@Override
	public void startGame(List<String> playerNames, GameType gameType, int beginnerPlayer)
	{
		trullAnnounced = false;
		cardIndex = 0;
	}

	@Override
	public void availableAnnouncements(List<Announcement> announcements)
	{
		for (Announcement announcement : announcements)
		{
			if (announcement.getContraLevel() == 0)
				return;
		}

		activateDelayed(3);
	}

	@Override
	public void announce(int player, Announcement announcement)
	{
		cancelActivation();
	}

	@Override
	public void announcePassz(int player)
	{
		cancelActivation();
	}
}
