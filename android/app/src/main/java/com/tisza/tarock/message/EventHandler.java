package com.tisza.tarock.message;

import com.tisza.tarock.*;
import com.tisza.tarock.card.*;

import java.util.*;

public interface EventHandler
{
	public void startGame(int myID, List<String> playerNames, GameType gameType);
	public void statistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, List<Integer> points);
	public void announce(int player, Announcement announcementContra);
	public void passz(int player);
	public void availableAnnouncements(List<Announcement> announcements);
	public void availableBids(List<Integer> bids);
	public void availableCalls(List<Card> cards);
	public void bid(int player, int bid);
	public void call(int player, Card card);
	public void cardsTaken(int winnerPlayer);
	public void cardsThrown(int player, PlayerCards thrownCards);
	public void changeDone(int player);
	public void phaseChanged(PhaseEnum phase);
	public void cardPlayed(int player, Card card);
	public void cardsChanged(List<Card> pc);
	public void skartTarock(int[] counts);
	public void turn(int player);
	public void wrongAction();
	public void pendingNewGame();
}
