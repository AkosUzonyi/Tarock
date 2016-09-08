package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class Facan extends AnnouncementBase
{
	private Card card;

	Facan(Card card)
	{
		this.card = card;
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		Round round = gi.gameplay.getRoundsPassed().get(0);
		int theCardPlayer = round.getCards().indexOf(card);
		if (theCardPlayer < 0) return Result.FAILED;
		
		PlayerPairs playerPairs = gi.calling.getPlayerPairs();
		
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
					if (round.getCards().get(opponentPlayer) instanceof TarockCard)
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
	
	public boolean canBeAnnounced(Announcing announcing, Team team)
	{
		return false;
	}
}
