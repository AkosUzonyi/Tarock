package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public abstract class TakeCards extends AnnouncementBase
{
	TakeCards(){}
	
	public Result isSuccessful(GameInstance gi, Team team)
	{
		List<Card> wonCards = new ArrayList<Card>();
		for (int player : gi.calling.getPlayerPairs().getPlayersInTeam(team))
		{
			wonCards.addAll(gi.gameplay.getWonCards(player));
		}
		wonCards.addAll(gi.changing.getSkartForTeam(Team.CALLER));
		wonCards.addAll(gi.changing.getSkartForTeam(Team.OPPONENT));
		
		return wonCards.containsAll(getCardsToTake()) ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
	
	public abstract boolean canBeSilent();
	protected abstract Collection<Card> getCardsToTake();
}
