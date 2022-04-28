package com.tisza.tarock.game;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;

public interface EventHandler
{
	default void startGame(GameType gameType, int beginnerPlayer) {}
	//default void statistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier) {}
	default void playerPoints(List<Integer> points, List<Integer> incrementPoints) {}
	default void announce(int player, Announcement announcementContra) {}
	default void announcePassz(int player) {}
	default void availableAnnouncements(List<Announcement> announcements) {}
	default void availableBids(List<Integer> bids) {}
	default void availableCalls(List<Card> cards) {}
	default void bid(int player, int bid) {}
	default void call(int player, Card card) {}
	default void cardsTaken(int winnerPlayer) {}
	default void throwCards(int player) {}
	default void foldDone(int player) {}
	default void phaseChanged(PhaseEnum phase) {}
	default void playCard(int player, Card card) {}
	default void cardsChanged(List<Card> pc, boolean canBeThrown) {}
	default void fold(int player, List<Card> cards) {}
	default void foldTarock(int[] counts) {}
	default void turn(int player) {}
	default void playerTeamInfo(int player, Team team) {}
	default void wrongAction() {}
	default void pendingNewGame() {}
	default void readyForNewGame(int player) {}
	default void chat(int userID, String message) {}
}
