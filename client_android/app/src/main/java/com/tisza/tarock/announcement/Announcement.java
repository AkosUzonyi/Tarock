package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameState gameState, Team team);
	public boolean canBeAnnounced(IAnnouncing announcing);
	public void onAnnounce(IAnnouncing announcing);
	public boolean canContra();
	public boolean isShownToUser();
	public boolean requireIdentification();
}
