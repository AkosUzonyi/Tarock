package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class Ultimo extends TrickAnnouncement
{
	private final int trickIndex;
	private final Card cardToTakeWith;
		
	Ultimo(int trickIndex, Card cardToTakeWith)
	{
		this.trickIndex = trickIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	@Override
	public String getID()
	{
		return "ultimo" + "C" + cardToTakeWith.getID() + "R" + trickIndex;
	}

	public final Card getCard()
	{
		return cardToTakeWith;
	}

	public final int getTrick()
	{
		return trickIndex;
	}

	@Override
	protected Result isSuccessful(Game game, Team team)
	{
		Trick trick = game.getTrick(trickIndex);
		PlayerSeat theCardPlayer = trick.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer == null) return Result.FAILED;
		
		if (game.getPlayerPairs().getTeam(theCardPlayer) != team)
		{
			return Result.FAILED;
		}
		else
		{
			if (trick.getWinner() == theCardPlayer)
			{
				return canBeSilent() ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
			}
			else
			{
				return canBeSilent() ? Result.FAILED_SILENT : Result.FAILED;
			}
		}
	}

	@Override
	protected boolean containsTrick(int trick)
	{
		return this.trickIndex == trick;
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		if (!(announcement instanceof Ultimo))
			return false;

		Ultimo otherUltimo = (Ultimo)announcement;

		return cardToTakeWith == otherUltimo.cardToTakeWith && trickIndex < otherUltimo.trickIndex;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
