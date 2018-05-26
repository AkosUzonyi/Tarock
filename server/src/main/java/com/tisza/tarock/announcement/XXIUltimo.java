package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.GameState;
import com.tisza.tarock.game.Team;

public class XXIUltimo extends Ultimo
{
	XXIUltimo(int roundIndex)
	{
		super(roundIndex, Card.getTarockCard(21));
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (int p : gameState.getPlayerPairs().getPlayersInTeam(team))
		{
			PlayerCards pc = gameState.getPlayerCards(p);
			if (pc.hasCard(Card.getTarockCard(22)))
			{
				return Result.DEACTIVATED;
			}
		}
		
		return super.isSuccessful(gameState, team);
	}

	@Override
	public int getPoints()
	{
		return getRoundIndex() == 8 ? 21 : (10 - getRoundIndex()) * 10;
	}
}
