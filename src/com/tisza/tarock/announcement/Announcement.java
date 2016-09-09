package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameInstance gi, Team team);
	public boolean canBeAnnounced(Announcing announcing, Team team);
	public void onAnnounce(Announcing announcing, Team team);
	public boolean canContra();
	public boolean isShownToUser();
}
