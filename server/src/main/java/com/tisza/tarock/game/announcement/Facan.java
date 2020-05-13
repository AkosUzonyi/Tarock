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
		PlayerPairs playerPairs = game.getPlayerPairs();

		Round round = game.getRound(0);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(getCard());

		if (theCardPlayer == null || playerPairs.getTeam(theCardPlayer) != team)
			return Result.FAILED;

		if (theCardPlayer == round.getWinner())
			return Result.SUCCESSFUL_SILENT;

		for (PlayerSeat opponentPlayer : playerPairs.getPlayersInTeam(team.getOther()))
			if (round.getCardByPlayer(opponentPlayer).doesBeat(getCard()))
				return Result.FAILED_SILENT;

		return Result.FAILED;
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
