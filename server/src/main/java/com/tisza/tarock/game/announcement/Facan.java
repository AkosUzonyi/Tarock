package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class Facan extends Ultimo
{
	Facan(Card card)
	{
		super(0, card);
	}

	@Override
	public GameType getGameType()
	{
		return GameType.MAGAS;
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		Round round = game.getRound(0);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(getCard());
		if (theCardPlayer == null) return Result.FAILED;
		
		PlayerPairs playerPairs = game.getPlayerPairs();
		
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
	protected boolean canBeSilent()
	{
		return true;
	}
}
