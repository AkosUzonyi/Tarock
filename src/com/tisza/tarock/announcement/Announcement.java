package com.tisza.tarock.announcement;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameInstance gi, Team team);
	public int getID();
	public boolean canBeAnnounced(Announcing announcing, Team team);
}
