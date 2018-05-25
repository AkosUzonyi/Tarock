package com.tisza.tarock.message;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;

import java.util.*;

public class BroadcastEventSender implements EventSender
{
	private List<EventSender> eventSenders = new ArrayList<>();

	public BroadcastEventSender(List<EventSender> eventSenders)
	{
		this.eventSenders.addAll(eventSenders);
	}

	@Override
	public void announce(int player, AnnouncementContra announcement)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announce(player, announcement);
		}
	}

	@Override
	public void announcePassz(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announcePassz(player);
		}
	}

	@Override
	public void bid(int player, int bid)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.bid(player, bid);
		}
	}

	@Override
	public void call(int player, Card card)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.call(player, card);
		}
	}

	@Override
	public void playCard(int player, Card card)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.playCard(player, card);
		}
	}

	@Override
	public void readyForNewGame(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.readyForNewGame(player);
		}
	}

	@Override
	public void throwCards(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.throwCards(player);
		}
	}

	@Override
	public void turn(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.turn(player);
		}
	}

	@Override
	public void startGame(int id, List<String> names)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.startGame(id, names);
		}
	}

	@Override
	public void playerCards(PlayerCards cards)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.playerCards(cards);
		}
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.phaseChanged(phase);
		}
	}

	@Override
	public void availabeBids(Collection<Integer> bids)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.availabeBids(bids);
		}
	}

	@Override
	public void availabeCalls(Collection<Card> cards)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.availabeCalls(cards);
		}
	}

	@Override
	public void cardsFromTalon(List<Card> cards)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.cardsFromTalon(cards);
		}
	}

	@Override
	public void changeDone(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.changeDone(player);
		}
	}

	@Override
	public void skartTarock(int[] counts)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.skartTarock(counts);
		}
	}

	@Override
	public void availableAnnouncements(List<AnnouncementContra> announcements)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.availableAnnouncements(announcements);
		}
	}

	@Override
	public void cardsTaken(int player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.cardsTaken(player);
		}
	}

	@Override
	public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
		}
	}

	@Override
	public void pendingNewGame()
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.pendingNewGame();
		}
	}
}
