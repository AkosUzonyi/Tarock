package com.tisza.tarock.message;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;

import java.util.*;

public class BroadcastEventSender implements EventSender
{
	private List<EventSender> eventSenders;

	public BroadcastEventSender()
	{
		setEventSenders(Collections.EMPTY_LIST);
	}

	public BroadcastEventSender(List<EventSender> eventSenders)
	{
		setEventSenders(eventSenders);
	}

	public void setEventSenders(List<EventSender> eventSenders)
	{
		this.eventSenders = eventSenders;
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcement)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announce(player, announcement);
		}
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announcePassz(player);
		}
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.bid(player, bid);
		}
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.call(player, card);
		}
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.playCard(player, card);
		}
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.readyForNewGame(player);
		}
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.throwCards(player);
		}
	}

	@Override
	public void turn(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.turn(player);
		}
	}

	@Override
	public void startGame(PlayerSeat id, List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.startGame(id, names, gameType, beginnerPlayer);
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
	public void changeDone(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.changeDone(player);
		}
	}

	@Override
	public void skartTarock(PlayerSeat.Map<Integer> counts)
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
	public void cardsTaken(PlayerSeat player)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.cardsTaken(player);
		}
	}

	@Override
	public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points, int pointMultiplier)
	{
		for (EventSender eventSender : eventSenders)
		{
			eventSender.announcementStatistics(selfGamePoints, opponentGamePoints, selfEntries, opponentEntries, sumPoints, points, pointMultiplier);
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
