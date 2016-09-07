package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameInstance game, Team team, boolean isSilent);
	public int getID();
	public boolean canBeAnnounced(Announcing announcing);
}
