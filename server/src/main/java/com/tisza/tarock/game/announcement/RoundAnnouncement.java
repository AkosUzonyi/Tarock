package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class RoundAnnouncement extends AnnouncementBase
{
	RoundAnnouncement() {}

	protected abstract boolean containsRound(int round);
	protected abstract boolean canOverrideAnnouncement(RoundAnnouncement announcement);

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();

		for (RoundAnnouncement other : Announcements.getRoundAnnouncements())
		{
			if (!announcing.isAnnounced(team, other))
				continue;

			if (other.canOverrideAnnouncement(this))
				return false;

			if (!announcing.getGameType().hasParent(GameType.MAGAS) && canOverrideAnnouncement(other))
				return false;

			if (hasCommonRoundWith(other) && !canOverrideAnnouncement(other))
				return false;
		}

		return super.canBeAnnounced(announcing);
	}

	private boolean hasCommonRoundWith(RoundAnnouncement other)
	{
		for (int round = 0; round < Game.ROUND_COUNT; round++)
		{
			if (containsRound(round) && other.containsRound(round))
				return true;
		}

		return false;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		super.onAnnounced(announcing);

		Team team = announcing.getCurrentTeam();

		for (RoundAnnouncement other : Announcements.getRoundAnnouncements())
		{
			if (announcing.isAnnounced(team, other) && canOverrideAnnouncement(other))
				announcing.clearAnnouncement(team, other);
		}
	}
}
