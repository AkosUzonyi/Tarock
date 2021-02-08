package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public abstract class TrickAnnouncement extends AnnouncementBase
{
	TrickAnnouncement() {}

	protected abstract boolean containsTrick(int trick);
	protected abstract boolean canOverrideAnnouncement(TrickAnnouncement announcement);

	@Override
	public boolean canBeAnnounced(IAnnouncing announcing)
	{
		Team team = announcing.getCurrentTeam();

		for (TrickAnnouncement other : Announcements.getTrickAnnouncements())
		{
			if (!announcing.isAnnounced(team, other))
				continue;

			if (other.canOverrideAnnouncement(this))
				return false;

			if (!announcing.getGameType().hasParent(GameType.MAGAS) && canOverrideAnnouncement(other))
				return false;

			if (hasCommonTrickWith(other) && !canOverrideAnnouncement(other))
				return false;
		}

		return super.canBeAnnounced(announcing);
	}

	private boolean hasCommonTrickWith(TrickAnnouncement other)
	{
		for (int trick = 0; trick < Game.ROUND_COUNT; trick++)
		{
			if (containsTrick(trick) && other.containsTrick(trick))
				return true;
		}

		return false;
	}

	@Override
	public void onAnnounced(IAnnouncing announcing)
	{
		super.onAnnounced(announcing);

		Team team = announcing.getCurrentTeam();

		for (TrickAnnouncement other : Announcements.getTrickAnnouncements())
		{
			if (announcing.isAnnounced(team, other) && canOverrideAnnouncement(other))
				announcing.clearAnnouncement(team, other);
		}
	}
}
