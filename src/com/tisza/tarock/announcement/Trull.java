package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Trull extends AnnouncementBase
{
	public Result isSuccessful(Gameplay gp, PlayerPairs pp, boolean callerTeam)
	{
		List<Card> wonCards = new ArrayList<Card>();
		//wonCards.addAll(gp.getPlayerStats().get(player0).cardsWon);
		//wonCards.addAll(gp.getPlayerStats().get(player1).cardsWon);
		boolean successful = wonCards.containsAll(Card.honors);
		return successful ? Result.SUCCESSFUL_SILENT : Result.FAILED;
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
