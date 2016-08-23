package com.tisza.tarock.announcement;

import com.tisza.tarock.game.*;

public abstract class AnnouncementBase implements Announcement
{
	public abstract boolean isSuccessful(Gameplay gp, int player0, int player1);
	public abstract int getPoints();
	
	public int calculatePoints(Gameplay gp, int player0, int player1)
	{
		return (isSuccessful(gp, player0, player1) ? 1 : -1) * getPoints();
	}	
	
	public boolean isSilent()
	{
		return false;
	}
	
	public boolean hasSilentPair()
	{
		return false;
	}
	
	public String getName()
	{
		throw new UnsupportedOperationException();
	}
}
