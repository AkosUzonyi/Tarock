package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public class PagatSasUltimo extends Ultimo
{
	PagatSasUltimo(int trickIndex, TarockCard cardToTakeWith)
	{
		super(trickIndex, cardToTakeWith);
	}

	@Override
	public GameType getGameType()
	{
		if (getCard().equals(Card.getTarockCard(2)))
			return GameType.MAGAS;

		switch (getTrick())
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
		//it just prevents silent pagatsas ultimo, when zaroparos is announced (zaroparos and pagatsas can't be announced together)
		if (game.getAnnouncementsState().isAnnounced(team, Announcements.zaroparos))
			return Result.FAILED;

		return super.isSuccessful(game, team);
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();
		PlayerSeat player = announcing.getCurrentPlayer();

		if (announcing.isAnnounced(team, Announcements.zaroparos))
			return false;

		if (announcing.getCards(player).getTarockCount() >= 8 && announcing.getTarockCountAnnounced(player) == null)
			return false;

		return super.canBeAnnounced(announcing);
	}

	@Override
	public boolean canContra(IAnnouncing announcing)
	{
		PlayerSeat player = announcing.getCurrentPlayer();
		if (announcing.getCards(player).getTarockCount() >= 8 && announcing.getTarockCountAnnounced(player) == null)
			return false;

		return super.canContra(announcing);
	}

	@Override
	public int getPoints()
	{
		return 10 * (9 - getTrick());
	}

	@Override
	public boolean canBeSilent()
	{
		return getTrick() == 8;
	}
}
