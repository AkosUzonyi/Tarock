package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class XXIFogas extends AnnouncementBase
{
	XXIFogas(){}

	@Override
	public AnnouncementID getID()
	{
		return new AnnouncementID("xxifogas");
	}

	@Override
	public GameType getGameType()
	{
		return GameType.PASKIEVICS;
	}

	@Override
	public int calculatePoints(GameState gameState, Team team)
	{
		int points = super.calculatePoints(gameState, team);

		if (gameState.getGameType() == GameType.ZEBI)
			points = points * 30 / 21; //a bit hacky, but hey, this rule is also hacky

		return points;
	}

	@Override
	public Result isSuccessful(GameState gameState, Team team)
	{
		PlayerPairs pp = gameState.getPlayerPairs();
		
		for (int i = 0; i < GameState.ROUND_COUNT; i++)
		{
			Round round = gameState.getRound(i);
			PlayerSeat skizPlayer = round.getPlayerOfCard(Card.getTarockCard(22));
			PlayerSeat XXIPlayer = round.getPlayerOfCard(Card.getTarockCard(21));
			if (skizPlayer == null || XXIPlayer == null) continue;
			
			Team skizTeam = pp.getTeam(skizPlayer);
			Team XXITeam = pp.getTeam(XXIPlayer);
			if (skizTeam == team && XXITeam != team)
			{
				return Result.SUCCESSFUL_SILENT;
			}
		}
		return Result.FAILED;
	}

	@Override
	public int getPoints()
	{
		return 42;
		//yes it is the answer to the ultimate question of life, the universe and everything
		//it's not a coincidence, and not a joke
		//42 is the points earned for XXIFogas
		//and the number of tarock cards... it's also 42
		//now go, and play tarock
	}
}
