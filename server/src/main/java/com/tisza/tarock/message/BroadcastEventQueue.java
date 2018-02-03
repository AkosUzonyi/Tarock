package com.tisza.tarock.message;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class BroadcastEventQueue implements EventQueue
{
	private List<EventQueue> eventQueues = new ArrayList<>();

	public BroadcastEventQueue(List<EventQueue> eventQueues)
	{
		this.eventQueues.addAll(eventQueues);
	}

	@Override
	public void announce(int player, AnnouncementContra announcement)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.announce(player, announcement);
		}
	}

	@Override
	public void announcePassz(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.announcePassz(player);
		}
	}

	@Override
	public void bid(int player, int bid)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.bid(player, bid);
		}
	}

	@Override
	public void call(int player, Card card)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.call(player, card);
		}
	}

	@Override
	public void playCard(int player, Card card)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.playCard(player, card);
		}
	}

	@Override
	public void readyForNewGame(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.readyForNewGame(player);
		}
	}

	@Override
	public void throwCards(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.throwCards(player);
		}
	}

	@Override
	public void turn(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.turn(player);
		}
	}

	@Override
	public void startGame(int id, List<String> names)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.startGame(id, names);
		}
	}

	@Override
	public void playerCards(PlayerCards cards)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.playerCards(cards);
		}
	}

	@Override
	public void phaseChanged(PhaseEnum phase)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.phaseChanged(phase);
		}
	}

	@Override
	public void availabeBids(Collection<Integer> bids)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.availabeBids(bids);
		}
	}

	@Override
	public void availabeCalls(Collection<Card> cards)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.availabeCalls(cards);
		}
	}

	@Override
	public void cardsFromTalon(List<Card> cards)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.cardsFromTalon(cards);
		}
	}

	@Override
	public void changeDone(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.changeDone(player);
		}
	}

	@Override
	public void skartTarock(int[] counts)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.skartTarock(counts);
		}
	}

	@Override
	public void availableAnnouncements(List<AnnouncementContra> announcements)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.availableAnnouncements(announcements);
		}
	}

	@Override
	public void cardsTaken(int player)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.cardsTaken(player);
		}
	}

	@Override
	public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points)
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points);
		}
	}

	@Override
	public void pendingNewGame()
	{
		for (EventQueue eventQueue : eventQueues)
		{
			eventQueue.pendingNewGame();
		}
	}
}
