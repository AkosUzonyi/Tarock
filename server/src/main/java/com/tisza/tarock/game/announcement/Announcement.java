package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public interface Announcement
{
	String getID();
	int calculatePoints(Game game, Team team);
	GameType getGameType();
	boolean canBeAnnounced(IAnnouncing announcing);
	void onAnnounced(IAnnouncing announcing);
	boolean canContra(IAnnouncing announcing);
	boolean requireIdentification();
	boolean shouldBeStored();
}
