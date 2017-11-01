package com.tisza.tarock.message;

import java.util.List;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.message.event.EventActionFailed;
import com.tisza.tarock.message.event.EventAnnouncementStatistics;
import com.tisza.tarock.message.event.EventActionFailed.Reason;
import com.tisza.tarock.message.event.EventAnnouncementStatistics.Entry;

public interface EventHandler
{
	public void startGame(int myID, List<String> playerNames);

	public void statistics(int selfGamePoints, int opponentGamePoints, List<Entry> selfEntries, List<Entry> opponentEntries, int sumPoints, int[] points);

	public void announce(int player, AnnouncementContra announcementContra);

	public void passz(int player);

	public void availableAnnouncements(List<AnnouncementContra> announcements);

	public void availableBids(List<Integer> bids);

	public void availableCalls(List<Card> cards);

	public void bid(int player, int bid);

	public void call(int player, Card card);

	public void cardsTaken(int winnerPlayer);

	public void cardsThrown(int player, PlayerCards thrownCards);

	public void changeDone(int player);

	public void phaseChanged(PhaseEnum phase);

	public void cardPlayed(int player, Card card);

	public void cardsChanged(PlayerCards pc);

	public void skartTarokk(int[] counts);

	public void turn(int player);

	public void wrongAction(Reason type);

	public void cardsFromTalon(List<Card> cards);

	public void pendingNewGame();
}
