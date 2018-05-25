package com.tisza.tarock.message;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.proto.ActionProto;

import java.util.Collection;
import java.util.List;

public interface EventSender
{
	void announce(int player, AnnouncementContra announcement);
	void announcePassz(int player);
	void bid(int player, int bid);
	void call(int player, Card card);
	void playCard(int player, Card card);
	void readyForNewGame(int player);
	void throwCards(int player);
	void turn(int player);
	void startGame(int id, List<String> names);
	void playerCards(PlayerCards cards);
	void phaseChanged(PhaseEnum phase);
	void availabeBids(Collection<Integer> bids);
	void availabeCalls(Collection<Card> cards);
	void cardsFromTalon(List<Card> cards);
	void changeDone(int player);
	void skartTarock(int[] counts);
	void availableAnnouncements(List<AnnouncementContra> announcements);
	void cardsTaken(int player);
	void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points);
	void pendingNewGame();
}
