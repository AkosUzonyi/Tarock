package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class Ultimo extends RoundAnnouncement
{
	private final int roundIndex;
	private final Card cardToTakeWith;
		
	Ultimo(int roundIndex, Card cardToTakeWith)
	{
		this.roundIndex = roundIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	@Override
	public String getID()
	{
		return "ultimo" + "C" + cardToTakeWith.getID() + "R" + roundIndex;
	}

	public final Card getCard()
	{
		return cardToTakeWith;
	}

	public final int getRound()
	{
		return roundIndex;
	}

	@Override
	protected Result isSuccessful(GameState gameState, Team team)
	{
		Round round = gameState.getRound(roundIndex);
		PlayerSeat theCardPlayer = round.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer == null) return Result.FAILED;
		
		if (gameState.getPlayerPairs().getTeam(theCardPlayer) != team)
		{
			return Result.FAILED;
		}
		else
		{
			if (round.getWinner() == theCardPlayer)
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
	protected boolean containsRound(int round)
	{
		return this.roundIndex == round;
	}

	@Override
	protected boolean canOverrideAnnouncement(RoundAnnouncement announcement)
	{
		if (!(announcement instanceof Ultimo))
			return false;

		Ultimo otherUltimo = (Ultimo)announcement;

		return cardToTakeWith == otherUltimo.cardToTakeWith && roundIndex < otherUltimo.roundIndex;
	}

	protected boolean canBeSilent()
	{
		return false;
	}
}
