package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class LastTricks extends TrickAnnouncement
{
	LastTricks(){}

	protected abstract int getTrickCount();
	protected abstract boolean isValidCard(Card card);
	protected abstract boolean isSameCategory(LastTricks otherAnnouncements);

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		for (int i = 0; i < getTrickCount(); i++)
		{
			int trickIndex = 8 - i;
			if (!isTrickOK(game, team, trickIndex))
			{
				return Result.FAILED;
			}
		}
		return Result.SUCCESSFUL;
	}

	private boolean isTrickOK(Game game, Team team, int trickIndex)
	{
		Trick trick = game.getTrick(trickIndex);
		PlayerSeat winnerPlayer = trick.getWinner();
		Card winnerCard = trick.getCardByPlayer(winnerPlayer);

		boolean isItUs = game.getPlayerPairs().getTeam(winnerPlayer) == team;
		boolean isValidCard = isValidCard(winnerCard);

		return isItUs && isValidCard;
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		if (announcement instanceof LastTricks)
		{
			LastTricks otherLastTricks = (LastTricks)announcement;
			if (isSameCategory(otherLastTricks) && getTrickCount() > otherLastTricks.getTrickCount())
				return true;
		}

		if (announcement instanceof Ultimo)
		{
			Ultimo ultimo = (Ultimo)announcement;
			if (containsTrick(ultimo.getTrick()) && isValidCard(ultimo.getCard()))
				return true;
		}

		return false;
	}

	@Override
	protected boolean containsTrick(int trick)
	{
		return trick >= Game.ROUND_COUNT - getTrickCount();
	}
}

