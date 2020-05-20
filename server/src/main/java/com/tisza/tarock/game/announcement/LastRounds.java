package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class LastRounds extends RoundAnnouncement
{
	LastRounds(){}

	protected abstract int getRoundCount();
	protected abstract boolean isValidCard(Card card);
	protected abstract boolean isSameCategory(LastRounds otherAnnouncements);

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		for (int i = 0; i < getRoundCount(); i++)
		{
			int roundIndex = 8 - i;
			if (!isRoundOK(game, team, roundIndex))
			{
				return Result.FAILED;
			}
		}
		return Result.SUCCESSFUL;
	}

	private boolean isRoundOK(Game game, Team team, int roundIndex)
	{
		Round round = game.getRound(roundIndex);
		PlayerSeat winnerPlayer = round.getWinner();
		Card winnerCard = round.getCardByPlayer(winnerPlayer);

		boolean isItUs = game.getPlayerPairs().getTeam(winnerPlayer) == team;
		boolean isValidCard = isValidCard(winnerCard);

		return isItUs && isValidCard;
	}

	@Override
	protected boolean canOverrideAnnouncement(RoundAnnouncement announcement)
	{
		if (announcement instanceof LastRounds)
		{
			LastRounds otherLastRounds = (LastRounds)announcement;
			if (isSameCategory(otherLastRounds) && getRoundCount() > otherLastRounds.getRoundCount())
				return true;
		}

		if (announcement instanceof Ultimo)
		{
			Ultimo ultimo = (Ultimo)announcement;
			if (containsRound(ultimo.getRound()) && isValidCard(ultimo.getCard()))
				return true;
		}

		return false;
	}

	@Override
	protected boolean containsRound(int round)
	{
		return round >= Game.ROUND_COUNT - getRoundCount();
	}
}

