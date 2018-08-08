package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public interface EventHandler
{
	default void startGame(int myID, List<String> playerNames, GameType gameType, int beginnerPlayer) {}
	default void statistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, List<Integer> points, int pointMultiplier) {}
	default void announce(int player, Announcement announcementContra) {}
	default void announcePassz(int player) {}
	default void availableAnnouncements(List<Announcement> announcements) {}
	default void availableBids(List<Integer> bids) {}
	default void availableCalls(List<Card> cards) {}
	default void bid(int player, int bid) {}
	default void call(int player, Card card) {}
	default void cardsTaken(int winnerPlayer) {}
	default void cardsThrown(int player, PlayerCards thrownCards) {}
	default void changeDone(int player) {}
	default void phaseChanged(PhaseEnum phase) {}
	default void cardPlayed(int player, Card card) {}
	default void cardsChanged(List<Card> pc) {}
	default void skartTarock(int[] counts) {}
	default void turn(int player) {}
	default void playerTeamInfo(int player, boolean callerTeam) {}
	default void wrongAction() {}
	default void pendingNewGame() {}
	default void readyForNewGame(int player) {}
	default void deleteGame() {}
}
