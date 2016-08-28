package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(Gameplay gp, PlayerPairs pp, Team team, boolean isSilent);
	public String getName();
	public int getID();
	public boolean isSilent();
}
