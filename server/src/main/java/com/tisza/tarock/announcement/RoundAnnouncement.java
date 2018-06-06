package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

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

			if (hasCommonRoundWith(other) && !canOverrideAnnouncement(other))
				return false;
		}

		return super.canBeAnnounced(announcing);
	}

	private boolean hasCommonRoundWith(RoundAnnouncement other)
	{
		for (int round = 0; round < GameSession.ROUND_COUNT; round++)
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
