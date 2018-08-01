package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

import java.util.*;

public interface EventSender
{
	default void announce(PlayerSeat player, AnnouncementContra announcement) {}
	default void announcePassz(PlayerSeat player) {}
	default void bid(PlayerSeat player, int bid) {}
	default void call(PlayerSeat player, Card card) {}
	default void playCard(PlayerSeat player, Card card) {}
	default void readyForNewGame(PlayerSeat player) {}
	default void throwCards(PlayerSeat player) {}
	default void turn(PlayerSeat player) {}
	default void startGame(PlayerSeat seat, List<String> names, GameType gameType, PlayerSeat beginnerPlayer) {}
	default void playerCards(PlayerCards cards) {}
	default void phaseChanged(PhaseEnum phase) {}
	default void availabeBids(Collection<Integer> bids) {}
	default void availabeCalls(Collection<Card> cards) {}
	default void changeDone(PlayerSeat player) {}
	default void skartTarock(PlayerSeat.Map<Integer> counts) {}
	default void availableAnnouncements(List<AnnouncementContra> announcements) {}
	default void cardsTaken(PlayerSeat player) {}
	default void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points, int pointMultiplier) {}
	default void pendingNewGame() {}
}
