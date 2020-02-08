package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.player.*;

import java.util.*;

public interface EventHandler
{
	default void announce(PlayerSeat player, AnnouncementContra announcement) {}
	default void announcePassz(PlayerSeat player) {}
	default void bid(PlayerSeat player, int bid) {}
	default void call(PlayerSeat player, Card card) {}
	default void playCard(PlayerSeat player, Card card) {}
	default void readyForNewGame(PlayerSeat player) {}
	default void throwCards(PlayerSeat player) {}
	default void turn(PlayerSeat player) {}
	default void playerTeamInfo(PlayerSeat player, Team team) {}
	default void startGame(GameType gameType, PlayerSeat beginnerPlayer) {}
	default void playerCards(PlayerCards cards) {}
	default void phaseChanged(PhaseEnum phase) {}
	default void availableBids(Collection<Integer> bids) {}
	default void availableCalls(Collection<Card> cards) {}
	default void changeDone(PlayerSeat player) {}
	default void skartTarock(PlayerSeatMap<Integer> counts) {}
	default void availableAnnouncements(List<AnnouncementContra> announcements) {}
	default void cardsTaken(PlayerSeat player) {}
	default void announcementStatistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier) {}
	default void playerPoints(int[] points) {}
	default void pendingNewGame() {}
	default void historyMode(boolean isHistory) {}
	default void chat(PlayerSeat player, String message) {}
}
