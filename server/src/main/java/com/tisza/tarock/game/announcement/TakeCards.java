package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;

public abstract class TakeCards extends AnnouncementBase
{
	TakeCards(){}

	protected abstract boolean hasToBeTaken(Card card);
	protected abstract boolean canBeSilent();
	
	@Override
	public Result isSuccessful(Game game, Team team)
	{
		for (PlayerSeat opponentPlayer : game.getPlayerPairs().getPlayersInTeam(team.getOther()))
			for (Card opponentCard : game.getWonCards(opponentPlayer))
				if (hasToBeTaken(opponentCard))
					return Result.FAILED;

		boolean volatHidesSilent = this != Announcements.volat && (game.getAnnouncementsState().isAnnounced(team, Announcements.volat) || Announcements.volat.isSuccessful(game, team) == Result.SUCCESSFUL_SILENT);
		return canBeSilent() && !volatHidesSilent ? Result.SUCCESSFUL_SILENT : Result.SUCCESSFUL;
	}

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();

		if (announcing.isAnnounced(team, Announcements.volat))
			return false;

		return super.canBeAnnounced(announcing);
	}
}
