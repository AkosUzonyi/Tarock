package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameInstance gi, Team team);
	public int getID();
	public boolean canBeAnnounced(Announcing announcing, Team team);
	public boolean isShownToUser();
}
