package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public class XXIUltimo extends Ultimo
{
	XXIUltimo(int roundIndex)
	{
		super(roundIndex, Card.getTarockCard(21));
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		for (PlayerSeat player : gameState.getPlayerPairs().getPlayersInTeam(team))
		{
			PlayerCards pc = gameState.getPlayerCards(player);
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
		return getRound() == 8 ? 21 : (10 - getRound()) * 10;
	}
}
