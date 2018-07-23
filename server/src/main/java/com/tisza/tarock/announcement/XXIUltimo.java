package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

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
		if (gameState.getAnnouncementsState().getXXIUltimoDeactivated(team))
			return Result.DEACTIVATED;
		
		return super.isSuccessful(gameState, team);
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		super.onAnnounced(announcing);

		Team team = announcing.getCurrentTeam();

		for (PlayerSeat player : announcing.getPlayerPairs().getPlayersInTeam(team))
		{
			if (announcing.getCards(player).hasCard(Card.getTarockCard(22)))
			{
				announcing.setXXIUltimoDeactivated(team);
			}
		}
	}

	@Override
	public int getPoints()
	{
		return getRound() == 8 ? 21 : (10 - getRound()) * 10;
	}
}
