package com.tisza.tarock.message;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public interface EventSender
{
	void announce(PlayerSeat player, AnnouncementContra announcement);
	void announcePassz(PlayerSeat player);
	void bid(PlayerSeat player, int bid);
	void call(PlayerSeat player, Card card);
	void playCard(PlayerSeat player, Card card);
	void readyForNewGame(PlayerSeat player);
	void throwCards(PlayerSeat player);
	void turn(PlayerSeat player);
	void startGame(PlayerSeat seat, List<String> names, GameType gameType);
	void playerCards(PlayerCards cards);
	void phaseChanged(PhaseEnum phase);
	void availabeBids(Collection<Integer> bids);
	void availabeCalls(Collection<Card> cards);
	void changeDone(PlayerSeat player);
	void skartTarock(PlayerSeat.Map<Integer> counts);
	void availableAnnouncements(List<AnnouncementContra> announcements);
	void cardsTaken(PlayerSeat player);
	void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points);
	void pendingNewGame();
}
