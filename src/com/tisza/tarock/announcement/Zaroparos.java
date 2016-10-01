package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Zaroparos extends AnnouncementBase
{
	Zaroparos(){}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		if (!isRoundOK(gi, team, 8, new TarockCard(1)))
			return Result.FAILED;
		
		if (!isRoundOK(gi, team, 7, new TarockCard(2)))
			return Result.FAILED;
		
		return Result.SUCCESSFUL;
	}
	
	private boolean isRoundOK(GameInstance gi, Team team, int roundIndex, Card cardToTakeWith)
	{
		Round round = gi.gameplay.getRoundsPassed().get(roundIndex);
		int theCardPlayer = round.getCards().indexOf(cardToTakeWith);
		if (theCardPlayer < 0) return false;
		
		if (gi.calling.getPlayerPairs().getTeam(theCardPlayer) != team)
		{
			return false;
		}
		else
		{
			return round.getWinner() == theCardPlayer;
		}
	}
	
	public boolean canBeAnnounced(Announcing announcing)
	{
		Announcement a0 = Announcements.ultimok.get(new TarockCard(1)).get(6);
		Announcement a1 = Announcements.ultimok.get(new TarockCard(2)).get(6);
		if (!a0.canBeAnnounced(announcing))
			return false;
		if  (!a1.canBeAnnounced(announcing))
			return false;
		
		return super.canBeAnnounced(announcing);
	}
	
	public void onAnnounce(Announcing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int t = 1; t <= 2; t++)
		{
			for (int ri = 8; ri > 6; ri--)
			{
				Announcement ultimo = Announcements.ultimok.get(new TarockCard(t)).get(ri);
				announcing.clearAnnouncement(team, ultimo);
			}
		}
	}

	protected int getPoints()
	{
		return 40;
	}
}