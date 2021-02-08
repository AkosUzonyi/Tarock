package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class KezbeVacak extends TrickAnnouncement
{
	private final int trickIndex;
	private final Card cardToTakeWith;
		
	KezbeVacak(int trickIndex, Card cardToTakeWith)
	{
		this.trickIndex = trickIndex;
		this.cardToTakeWith = cardToTakeWith;
	}

	@Override
	public String getID()
	{
		return "kezbevacak" + "R" + trickIndex;
	}

	public int getTrick()
	{
		return trickIndex;
	}

	public Card getCard()
	{
		return cardToTakeWith;
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ILLUSZTRALT;
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		Trick trick = game.getTrick(trickIndex);
		PlayerSeat theCardPlayer = trick.getPlayerOfCard(cardToTakeWith);
		if (theCardPlayer == null) return Result.FAILED;
		
		if (game.getPlayerPairs().getTeam(theCardPlayer) != team)
			return Result.FAILED;
		
		if (trick.getWinner() != theCardPlayer)
			return Result.FAILED;
		
		for (int i = 0; i < trickIndex; i++)
		{
			trick = game.getTrick(i);
			PlayerSeat winner = trick.getWinner();
			
			if (game.getPlayerPairs().getTeam(winner) != team)
				return Result.FAILED;
		}
		
		return Result.SUCCESSFUL;
	}

	@Override
	protected boolean containsTrick(int trick)
	{
		return this.trickIndex == trick;
	}

	@Override
	protected boolean canOverrideAnnouncement(TrickAnnouncement announcement)
	{
		return false;
	}

	@Override
	public int getPoints()
	{
		return 10;
	}
}
