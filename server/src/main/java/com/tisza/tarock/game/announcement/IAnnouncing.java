package com.tisza.tarock.game.announcement;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

public interface IAnnouncing
{
	boolean isAnnounced(Team team, Announcement a);
	void setContraLevel(Team team, Announcement a, int level);
	int getContraLevel(Team team, Announcement a);
	void clearAnnouncement(Team team, Announcement announcement);

	void setXXIUltimoDeactivated(Team team);
	boolean getXXIUltimoDeactivated(Team team);


	void announceTarockCount(PlayerSeat player, TarockCount announcement);
	TarockCount getTarockCountAnnounced(PlayerSeat player);

	PlayerSeat getCurrentPlayer();
	Team getCurrentTeam();
	PlayerPairs getPlayerPairs();
	boolean canAnnounce(AnnouncementContra a);
	PlayerCards getCards(PlayerSeat player);
	PlayerSeat getPlayerToAnnounceSolo();
	GameType getGameType();
}
