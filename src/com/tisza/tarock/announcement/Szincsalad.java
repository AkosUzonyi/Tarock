package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Szincsalad extends AnnouncementBase
{
	private int suit;
	private boolean big;
		
	Szincsalad(int suit, boolean big)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException();
		
		this.suit = suit;
		this.big = big;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		for (int i = 0; i < (big ? 3 : 2); i++)
		{
			int roundIndex = 8 - i;
			if (isRoundOK(gi, team, roundIndex))
			{
				return Result.SUCCESSFUL;
			}
		}
		return Result.FAILED;
	}
	
	private boolean isRoundOK(GameInstance gi, Team team, int roundIndex)
	{
		Round round = gi.gameplay.getRoundsPassed().get(roundIndex);
		for (int p = 0; p < 4; p++)
		{
			Card card = round.getCards().get(p);
			boolean isItUs = gi.calling.getPlayerPairs().getTeam(p) == team;
			boolean isCorrectSuit = card instanceof SuitCard && ((SuitCard)card).getSuit() == suit;
			boolean isWon = round.getWinner() == p;
			if (isItUs && isCorrectSuit && isWon)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		if (!big && announcing.isAnnounced(team, Announcements.nagyszincsaladok[suit]))
			return false;
		
		return super.canBeAnnounced(announcing, team);
	}
	
	public void onAnnounce(Announcing announcing, Team team)
	{
		if (big)
		{
			announcing.clearAnnouncement(team, Announcements.kisszincsaladok[suit]);
		}
	}

	protected int getPoints()
	{
		return big ? 100 : 60;
	}
}
