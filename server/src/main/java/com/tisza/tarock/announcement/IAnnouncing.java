package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

public interface IAnnouncing
{
	public boolean isAnnounced(Team team, Announcement a);
	public void setContraLevel(Team team, Announcement a, int level);
	public int getContraLevel(Team team, Announcement a);
	public void clearAnnouncement(Team team, Announcement announcement);

	public void setXXIUltimoDeactivated(Team team);
	public boolean getXXIUltimoDeactivated(Team team);

	public PlayerSeat getCurrentPlayer();
	public Team getCurrentTeam();
	public PlayerPairs getPlayerPairs();
	public boolean canAnnounce(AnnouncementContra a);
	public PlayerCards getCards(PlayerSeat player);
	public PlayerSeat getPlayerToAnnounceSolo();
}
