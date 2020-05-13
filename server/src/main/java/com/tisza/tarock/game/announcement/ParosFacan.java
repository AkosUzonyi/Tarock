package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;

public class ParosFacan extends RoundAnnouncement
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
		return GameType.MAGAS;
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

		if (xxiPlayer == null || playerPairs.getTeam(xxiPlayer) != team)
			return Result.FAILED;

		if (pagatPlayer == null && sasPlayer == null)
			return Result.FAILED;

		if (skizPlayer != null && playerPairs.getTeam(xxiPlayer) != team)
			return Result.FAILED_SILENT;

		boolean canBeSilent;

		if (game.getGameType().hasParent(GameType.ZEBI))
			canBeSilent = round.getFirstCard() instanceof SuitCard && playerPairs.getTeam(round.getBeginnerPlayer()) == team.getOther();
		else
			canBeSilent = pagatPlayer != null && playerPairs.getTeam(pagatPlayer) == team.getOther() || sasPlayer != null && playerPairs.getTeam(sasPlayer) == team.getOther();

		return canBeSilent ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
	}

	@Override
	protected int getPoints()
	{
		return 60;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		return announcing.getPlayerPairs().getTeam(PlayerSeat.SEAT0) != announcing.getCurrentTeam();
	}

	@Override
	protected boolean containsRound(int round)
	{
		return round == 0;
	}

	@Override
	protected boolean canOverrideAnnouncement(RoundAnnouncement announcement)
	{
		return announcement instanceof PagatSasUltimo || announcement instanceof Facan || announcement instanceof XXIUltimo || announcement instanceof Zaroparos || announcement == Announcements.kismadar;
	}
}
