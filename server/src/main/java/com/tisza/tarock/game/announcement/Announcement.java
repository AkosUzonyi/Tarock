package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

public interface Announcement
{
	public String getID();
	public int calculatePoints(GameState gameState, Team team);
	public GameType getGameType();
	public boolean canBeAnnounced(IAnnouncing announcing);
	public void onAnnounced(IAnnouncing announcing);
	public boolean canContra();
	public boolean requireIdentification();
	public boolean shouldBeStored();
}
