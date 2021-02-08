package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;

public class ParosFacan extends TrickAnnouncement
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

		Trick trick = game.getTrick(0);
		PlayerSeat pagatPlayer = trick.getPlayerOfCard(Card.getTarockCard(1));
		PlayerSeat sasPlayer = trick.getPlayerOfCard(Card.getTarockCard(2));
		PlayerSeat xxiPlayer = trick.getPlayerOfCard(Card.getTarockCard(21));

		if (xxiPlayer == null || playerPairs.getTeam(xxiPlayer) != team)
			return Result.FAILED;

		if (pagatPlayer == null && sasPlayer == null)
			return Result.FAILED;

		boolean canBeSilent;
		if (game.getGameType().hasParent(GameType.ZEBI))
			canBeSilent = trick.getFirstCard() instanceof SuitCard && playerPairs.getTeam(trick.getBeginnerPlayer()) == team.getOther();
		else
			canBeSilent = pagatPlayer != null && playerPairs.getTeam(pagatPlayer) == team.getOther() || sasPlayer != null && playerPairs.getTeam(sasPlayer) == team.getOther();

		if (trick.getWinner() != xxiPlayer)
			return canBeSilent ? Result.FAILED_SILENT : Result.FAILED;

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
	protected boolean containsTrick(int trick)
	{
		return trick == 0;
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		return announcement instanceof PagatSasUltimo || announcement instanceof Facan || announcement instanceof XXIUltimo || announcement instanceof Zaroparos || announcement == Announcements.kismadar;
	}
}
