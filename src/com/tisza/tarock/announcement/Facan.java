package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.IAnnouncing;
import com.tisza.tarock.game.PlayerPairs;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public class Facan extends AnnouncementBase
{
	private Card card;

	Facan(Card card)
	{
		this.card = card;
	}

	public Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(0);
		int theCardPlayer = round.getPlayerOfCard(card);
		if (theCardPlayer < 0) return Result.FAILED;
		
		PlayerPairs playerPairs = gameState.getPlayerPairs();
		
		if (playerPairs.getTeam(theCardPlayer) != team)
		{
			return Result.FAILED;
		}
		else
		{
			int winnerPlayer = round.getWinner();
			
			if (winnerPlayer == theCardPlayer)
			{
				return Result.SUCCESSFUL_SILENT;
			}
			else
			{
				for (int opponentPlayer : playerPairs.getPlayersInTeam(team.getOther()))
				{
					if (round.getCardByPlayer(opponentPlayer) instanceof TarockCard)
					{
						return Result.FAILED_SILENT;
					}
				}
				return Result.FAILED;
			}
		}
	}

	protected int getPoints()
	{
		return 10;
	}
	
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return false;
	}
}
