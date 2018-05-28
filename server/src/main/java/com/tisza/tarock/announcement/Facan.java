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

	@Override
	public String getName()
	{
		return "facan";
	}

	@Override
	public Card getCard()
	{
		return card;
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(0);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(card);
		if (theCardPlayer == null) return Result.FAILED;
		
		PlayerPairs playerPairs = gameState.getPlayerPairs();
		
		if (playerPairs.getTeam(theCardPlayer) != team)
		{
			return Result.FAILED;
		}
		else
		{
			PlayerSeat winnerPlayer = round.getWinner();
			
			if (winnerPlayer == theCardPlayer)
			{
				return Result.SUCCESSFUL_SILENT;
			}
			else
			{
				for (PlayerSeat opponentPlayer : playerPairs.getPlayersInTeam(team.getOther()))
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

	@Override
	protected int getPoints()
	{
		return 50;
	}

	@Override
	protected int getSilentPoints()
	{
		return 5;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return false;
	}
}
