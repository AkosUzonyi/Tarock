package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Trull extends AnnouncementBase
{
	Trull(){}
	
	public Result isSuccessful(GameInstance gi, Team team)
	{
		List<Card> wonCards = new ArrayList<Card>();
		for (int player : gi.calling.getPlayerPairs().getPlayersInTeam(team))
		{
			wonCards.addAll(gi.gameplay.getWonCards(player));
		}
		return wonCards.containsAll(Card.honors) ? Result.SUCCESSFUL_SILENT : Result.FAILED;
	}

	public int getPoints(int winnerBid)
	{
		return 2;
	}

	public int getID()
	{
		return 1;
	}
}
