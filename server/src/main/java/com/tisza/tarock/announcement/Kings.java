package com.tisza.tarock.announcement;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.SuitCard;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Round;
import com.tisza.tarock.game.Team;

public class Kings extends AnnouncementBase
{
	private static final int[] points = new int[]{8, 60, 100};
	
	private int count;
		
	Kings(int count)
	{
		if (count < 1 || count >= 4)
			throw new IllegalArgumentException();
		
		this.count = count;
	}

	@Override
	public String getName()
	{
		switch (count)
		{
			case 1:
				return "kiralyultimo";
			case 2:
				return "ketkiralyok";
			case 3:
				return "haromkiralyok";
			default:
				throw new Error();
		}
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (int i = 0; i < count; i++)
		{
			int roundIndex = 8 - i;
			if (!isRoundOK(gameState, team, roundIndex))
			{
				return Result.FAILED;
			}
		}
		return Result.SUCCESSFUL;
	}
	
	private boolean isRoundOK(GameState gameState, Team team, int roundIndex)
	{
		Round round = gameState.getRound(roundIndex);
		int winnerPlayer = round.getWinner();
		Card winnerCard = round.getCardByPlayer(winnerPlayer);

		boolean isItUs = gameState.getPlayerPairs().getTeam(winnerPlayer) == team;
		boolean isKing = winnerCard instanceof SuitCard && ((SuitCard)winnerCard).getValue() == 5;

		return isItUs && isKing;
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int i = 3; i >= count; i--)
		{
			if (announcing.isAnnounced(team, Announcements.kings[i - 1]))
			{
				return false;
			}
		}
		
		return super.canBeAnnounced(announcing);
	}
	
	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		
		for (int i = 1; i < count; i++)
		{
			announcing.clearAnnouncement(team, Announcements.kings[i - 1]);
		}
	}

	@Override
	protected int getPoints()
	{
		return points[count - 1];
	}
}
