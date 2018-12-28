package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public interface ActionHandler
{
	default void requestHistory(PlayerSeat player, EventSender eventSender) {}
	default void announce(PlayerSeat player, AnnouncementContra announcementContra) {}
	default void announcePassz(PlayerSeat player) {}
	default void bid(PlayerSeat player, int bid) {}
	default void call(PlayerSeat player, Card card) {}
	default void change(PlayerSeat player, List<Card> cards) {}
	default void playCard(PlayerSeat player, Card card) {}
	default void readyForNewGame(PlayerSeat player) {}
	default void throwCards(PlayerSeat player) {}
}
