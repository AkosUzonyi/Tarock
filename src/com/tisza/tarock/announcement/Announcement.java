package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameHistory gh, boolean callerTeam, boolean isSilent);
	public String getName();
	public int getID();
	public boolean isSilent();
}
