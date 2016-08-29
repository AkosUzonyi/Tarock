package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Trull extends AnnouncementBase
{
	Trull(){}
	
	public Result isSuccessful(Gameplay gp, PlayerPairs pp, Team team)
	{
		List<Card> wonCards = new ArrayList<Card>();
		for (int player : pp.getPlayersInTeam(team))
		{
			wonCards.addAll(gp.getWonCards(player));
		}
		return wonCards.containsAll(Card.honors) ? Result.SUCCESSFUL_SILENT : Result.FAILED;
	}

	public int getPoints()
	{
		return 2;
	}

	public int getID()
	{
		return 1;
	}
}
