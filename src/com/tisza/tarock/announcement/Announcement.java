package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(Gameplay gp, int player0, int player1);
	public String getName();
	public int getID();
	public boolean isSilent();
}
