package com.tisza.tarock.game;

import com.tisza.tarock.announcement.Announcement;
import com.tisza.tarock.card.PlayerCards;

public interface IAnnouncing
{
	public boolean isAnnounced(Team team, Announcement a);
	public void setContraLevel(Team team, Announcement a, int level);
	public int getContraLevel(Team team, Announcement a);
	public void clearAnnouncement(Team team, Announcement announcement);

	public int getCurrentPlayer();
	public Team getCurrentTeam();
	public boolean canAnnounce(AnnouncementContra a);
	public PlayerCards getCards(int player);
	public int getPlayerToAnnounceSolo();
}
