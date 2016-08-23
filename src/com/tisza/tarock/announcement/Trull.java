package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Trull extends AnnouncementBase
{
	public boolean isSuccessful(Gameplay gp, int player0, int player1)
	{
		List<Card> wonCards = new ArrayList<Card>();
		wonCards.addAll(gp.getPlayerStats().get(player0).cardsWon);
		wonCards.addAll(gp.getPlayerStats().get(player1).cardsWon);
		return wonCards.containsAll(Card.honors);
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
