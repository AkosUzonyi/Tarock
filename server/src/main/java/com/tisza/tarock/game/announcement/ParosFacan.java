package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;

public class ParosFacan extends AnnouncementBase
{
	ParosFacan() {}

	@Override
	public String getID()
	{
		return "parosfacan";
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		PlayerPairs playerPairs = game.getPlayerPairs();

		Round round = game.getRound(0);
		PlayerSeat pagatPlayer = round.getPlayerOfCard(Card.getTarockCard(1));
		PlayerSeat sasPlayer = round.getPlayerOfCard(Card.getTarockCard(2));
		PlayerSeat xxiPlayer = round.getPlayerOfCard(Card.getTarockCard(21));
		PlayerSeat skizPlayer = round.getPlayerOfCard(Card.getTarockCard(22));

		if (!(round.getFirstCard() instanceof SuitCard) || playerPairs.getTeam(round.getBeginnerPlayer()) == team)
			return Result.FAILED;

		if (xxiPlayer == null || playerPairs.getTeam(xxiPlayer) != team)
			return Result.FAILED;

		if (pagatPlayer == null && sasPlayer == null)
			return Result.FAILED;

		if (skizPlayer != null && playerPairs.getTeam(xxiPlayer) != team)
			return Result.FAILED_SILENT;

		return Result.SUCCESSFUL_SILENT;
	}

	@Override
	protected int getPoints()
	{
		return 60;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return false;
	}
}
