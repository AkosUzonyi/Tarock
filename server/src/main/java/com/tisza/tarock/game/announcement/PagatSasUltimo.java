package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class PagatSasUltimo extends Ultimo
{
	PagatSasUltimo(int roundIndex, TarockCard cardToTakeWith)
	{
		super(roundIndex, cardToTakeWith);
	}

	@Override
	public GameType getGameType()
	{
		if (getCard().equals(Card.getTarockCard(2)))
			return GameType.MAGAS;

		switch (getRound())
		{
			case 8:         return GameType.PASKIEVICS;
			case 7:         return GameType.ILLUSZTRALT;
			case 6: case 5: return GameType.MAGAS;
		}
		throw new RuntimeException();
	}

	@Override
	public Result isSuccessful(Game game, Team team)
	{
		Result zaroparosSuccessful = Announcements.zaroparos.isSuccessful(game, team);

		if (zaroparosSuccessful == Result.SUCCESSFUL)
		{
			//it just prevents silent pagatsas ultimo, when zaroparos is successful (zaroparos and pagatsas can't be announced together)
			return Result.FAILED;
		}
		
		return super.isSuccessful(game, team);
	}
	
	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		PlayerSeat player = announcing.getCurrentPlayer();
		
		if (announcing.isAnnounced(team, Announcements.zaroparos))
			return false;
		
		if (getRound() == 8 && announcing.getCards(player).getTarockCount() >= 8 && announcing.getTarockCountAnnounced(player) == null)
			return false;

		return super.canBeAnnounced(announcing);
	}

	@Override
	public int getPoints()
	{
		return 10 * (9 - getRound());
	}

	@Override
	public boolean canBeSilent()
	{
		return getRound() == 8;
	}
}
