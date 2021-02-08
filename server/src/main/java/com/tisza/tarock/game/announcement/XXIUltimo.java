package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class XXIUltimo extends Ultimo
{
	XXIUltimo(int trickIndex)
	{
		super(trickIndex, Card.getTarockCard(21));
	}

	@Override
	public GameType getGameType()
	{
		return GameType.ZEBI;
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		if (game.getAnnouncementsState().getXXIUltimoDeactivated(team))
			return Result.DEACTIVATED;
		
		return super.isSuccessful(game, team);
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
		return getTrick() == 8 ? 21 : (10 - getTrick()) * 10;
	}
}
