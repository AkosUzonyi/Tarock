package com.tisza.tarock.announcement;

import com.tisza.tarock.announcement.AnnouncementBase.Result;
import com.tisza.tarock.game.*;

public class XXIFogas
{
	public Result isSuccessful(GameHistory gh, int player0, int player1)
	{
		Gameplay gp = gh.gameplay;
		return null;
	}

	public int getPoints()
	{
		return 2;
	}

	public int getID()
	{
		return 1;
	}
	
	public boolean hasSilentPair()
	{
		return true;
	}
}
