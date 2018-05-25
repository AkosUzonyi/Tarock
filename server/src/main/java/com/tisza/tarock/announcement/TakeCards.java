package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Team;

import java.util.*;

public abstract class TakeCards extends AnnouncementBase
{
	TakeCards(){}

	protected abstract boolean hasToBeTaken(Card card);
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

		for (Card card : Card.getAll())
		{
			if (hasToBeTaken(card) && !wonCards.contains(card))
			{
				return Result.FAILED;
			}
		}
		
		return canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
	}
}
