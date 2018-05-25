package com.tisza.tarock.player.random;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.player.*;

import java.util.*;
import java.util.concurrent.*;

public class RandomPlayer implements Player
{
	private final String name;
	private final int delay;
	private EventSender eventSender = new MyEventSender();
	private Random rnd = new Random();

	private int playerID;
	private BlockingQueue<Action> actionQueue;

	public RandomPlayer(String name)
	{
		this(name, 700);
	}

	public RandomPlayer(String name, int delay)
	{
		this.name = name;
		this.delay = delay;
	}

	@Override
	public String getName()
	{
		return "<" + name + ">";
	}

	@Override
	public EventSender getEventSender()
	{
		return eventSender;
	}

	@Override
	public void onJoinedToGame(BlockingQueue<Action> actionQueue, int playerID)
	{
		this.actionQueue = actionQueue;
		this.playerID = playerID;
	}

	@Override
	public void onDisconnectedFromGame()
	{
		actionQueue = null;
	}

	private void enqueueAction(Action action)
	{
		if (actionQueue == null)
			throw new IllegalStateException("no action queue");

		actionQueue.add(action);
	}

	private void enqueueActionDelayed(Action action)
	{
		try
		{
			Thread.sleep(delay);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		enqueueAction(action);
	}

	private class MyEventSender implements EventSender
	{
		private PlayerCards myCards;
		private PhaseEnum phase;

		private <T> T chooseRandom(Collection<T> from)
		{
			int n = rnd.nextInt(from.size());
			Iterator<T> it = from.iterator();
			for (int i = 0; i < n; i++)
			{
				it.next();
			}
			return it.next();
		}

		@Override public void announce(int player, AnnouncementContra announcement) {}
		@Override public void announcePassz(int player) {}
		@Override public void bid(int player, int bid) {}
		@Override public void call(int player, Card card) {}

		private Card currentFirstCard = null;
		private int cardsInRound;

		@Override
		public void playCard(int player, Card card)
		{
			if (cardsInRound == 0)
				currentFirstCard = card;

			cardsInRound++;

			if (cardsInRound == 4)
				currentFirstCard = null;

			cardsInRound %= 4;
		}

		@Override public void readyForNewGame(int player) {}
		@Override public void throwCards(int player) {}

		@Override
		public void turn(int player)
		{
			if (phase == PhaseEnum.GAMEPLAY && player == playerID)
			{
				Card cardToPlay = chooseRandom(myCards.getPlaceableCards(currentFirstCard));
				myCards.removeCard(cardToPlay);
				enqueueActionDelayed(handler -> handler.playCard(playerID, cardToPlay));
			}
		}

		@Override public void startGame(int id, List<String> names)
		{
			playerID = id;
		}

		@Override
		public void playerCards(PlayerCards cards)
		{
			myCards = cards.clone();
		}

		@Override
		public void phaseChanged(PhaseEnum phase)
		{
			this.phase = phase;
		}

		@Override
		public void availabeBids(Collection<Integer> bids)
		{
			enqueueActionDelayed(handler -> handler.bid(playerID, chooseRandom(bids)));
		}

		@Override
		public void availabeCalls(Collection<Card> cards)
		{
			enqueueActionDelayed(handler -> handler.call(playerID, chooseRandom(cards)));
		}

		@Override
		public void cardsFromTalon(List<Card> cards)
		{
			List<Card> cardsToSkart = myCards.filter(new SkartableCardFilter()).subList(0, cards.size());
			myCards.getCards().removeAll(cardsToSkart);
			myCards.getCards().addAll(cards);
			enqueueAction(handler -> handler.change(playerID, cardsToSkart));
		}

		@Override public void changeDone(int player) {}
		@Override public void skartTarock(int[] counts) {}

		@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			if (announcements.contains(new AnnouncementContra(Announcements.hkp, 0)))
				enqueueActionDelayed(handler -> handler.announce(playerID, new AnnouncementContra(Announcements.hkp, 0)));

			while (!announcements.isEmpty() && rnd.nextFloat() < 0.2)
			{
				enqueueActionDelayed(handler -> handler.announce(playerID, chooseRandom(announcements)));
			}
			enqueueActionDelayed(handler -> handler.announcePassz(playerID));
		}

		@Override public void cardsTaken(int player) {}
		@Override public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points) {}

		@Override
		public void pendingNewGame()
		{
			enqueueAction(handler -> handler.readyForNewGame(playerID));
		}
	}
}
