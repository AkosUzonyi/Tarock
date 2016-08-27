package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(Gameplay gp, PlayerPairs pp, boolean callerTeam, boolean isSilent);
	public String getName();
	public int getID();
	public boolean isSilent();
}
