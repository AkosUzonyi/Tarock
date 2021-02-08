package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public interface ActionHandler
{
	boolean announce(PlayerSeat player, AnnouncementContra announcementContra);
	boolean announcePassz(PlayerSeat player);
	boolean bid(PlayerSeat player, int bid);
	boolean call(PlayerSeat player, Card card);
	boolean fold(PlayerSeat player, List<Card> cards);
	boolean playCard(PlayerSeat player, Card card);
	boolean readyForNewGame(PlayerSeat player);
	boolean throwCards(PlayerSeat player);
}
