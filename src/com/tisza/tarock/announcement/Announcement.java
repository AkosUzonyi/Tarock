package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameInstance gi, Team team);
	public boolean canBeAnnounced(Announcing announcing);
	public void onAnnounce(Announcing announcing);
	public boolean canContra();
	public boolean isShownToUser();
	public boolean requireIdentification();
}
