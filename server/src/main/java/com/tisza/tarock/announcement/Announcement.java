package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public interface Announcement
{
	public int calculatePoints(GameState gameState, Team team);
	public GameType getGameType();
	public boolean canBeAnnounced(IAnnouncing announcing);
	public void onAnnounced(IAnnouncing announcing);
	public boolean canContra();
	public boolean requireIdentification();
	public boolean shouldBeStored();

	public String getName();
	public default int getSuit() { return -1; }
	public default Card getCard() { return null; }
	public default int getRound() { return -1; }

	public default AnnouncementID getID()
	{
		AnnouncementID id = new AnnouncementID(getName());

		if (getSuit() >= 0)
			id.setSuit(getSuit());
		if (getCard() != null)
			id.setCard(getCard());
		if (getRound() >= 0)
			id.setRound(getRound());

		return id;
	}
}
