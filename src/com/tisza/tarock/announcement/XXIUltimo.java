package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIUltimo extends TakeRoundWithCard
{
	XXIUltimo(int roundIndex)
	{
		super(roundIndex, new TarockCard(21));
	}

	public Result isSuccessful(GameInstance gi, Team team)
	{
		for (int p : gi.calling.getPlayerPairs().getPlayersInTeam(team))
		{
			PlayerCards pc = gi.changing.getCardsAfter().getPlayerCards(p);
			if (pc.hasCard(new TarockCard(22)))
			{
				return Result.DEACTIVATED;
			}
		}
		
		return super.isSuccessful(gi, team);
	}

	public int getPoints(int winnerBid)
	{
		return getRoundIndex() == 8 ? 21 : (10 - getRoundIndex()) * 10;
	}
}
