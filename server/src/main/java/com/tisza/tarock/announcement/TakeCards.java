package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public abstract class TakeCards extends AnnouncementBase
{
	TakeCards(){}
	
	protected abstract Collection<Card> getCardsToTake();
	protected abstract boolean canBeSilent();
	
	public Result isSuccessful(GameState gameState, Team team)
	{
		Collection<Card> wonCards = new ArrayList<Card>();
		for (int player : gameState.getPlayerPairs().getPlayersInTeam(team))
		{
			wonCards.addAll(gameState.getWonCards(player));
		}
		wonCards.addAll(gameState.getSkartForTeam(Team.CALLER));
		wonCards.addAll(gameState.getSkartForTeam(Team.OPPONENT));
		
		return wonCards.containsAll(getCardsToTake()) ? (canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL) : Result.FAILED;
	}
}
