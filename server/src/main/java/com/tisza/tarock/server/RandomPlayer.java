package com.tisza.tarock.server;

import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.concurrent.*;

public class RandomPlayer implements Player
{
	private final String name;
	private final ScheduledExecutorService executorService;
	private final int delay, extraDelay;
	private EventSender eventSender = new MyEventSender();
	private Random rnd = new Random();

	private PlayerSeat seat;
	private ActionHandler actionHandler;

	public RandomPlayer(String name, ScheduledExecutorService executorService)
	{
		this(name, executorService, 0, 0);
	}

	public RandomPlayer(String name, ScheduledExecutorService executorService, int delay, int extraDelay)
	{
		this.name = name;
		this.executorService = executorService;
		this.delay = delay;
		this.extraDelay = extraDelay;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public EventSender getEventSender()
	{
		return eventSender;
	}

	@Override
	public void onAddedToGame(ActionHandler actionHandler, PlayerSeat seat)
	{
		this.actionHandler = actionHandler;
		this.seat = seat;
	}

	@Override
	public void onRemovedFromGame()
	{
		actionHandler = null;
		seat = null;
	}

	private void doAction(Action action)
	{
		if (actionHandler != null)
			action.handle(actionHandler);
	}

	private void enqueueAction(Action action)
	{
		executorService.execute(() -> doAction(action));
	}

	private void enqueueActionDelayed(Action action)
	{
		executorService.schedule(() -> doAction(action), delay, TimeUnit.MILLISECONDS);
	}

	private void enqueueActionExtraDelayed(Action action)
	{
		executorService.schedule(() -> doAction(action), extraDelay, TimeUnit.MILLISECONDS);
	}

	private class MyEventSender implements EventSender
	{
		private PlayerCards myCards;
		private PhaseEnum phase;
		private GameType gameType;

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

		@Override public void announce(PlayerSeat player, AnnouncementContra announcement) {}
		@Override public void announcePassz(PlayerSeat player) {}
		@Override public void bid(PlayerSeat player, int bid) {}
		@Override public void call(PlayerSeat player, Card card) {}

		private Card currentFirstCard = null;
		private int cardsInRound;

		@Override
		public void playCard(PlayerSeat player, Card card)
		{
			if (cardsInRound == 0)
				currentFirstCard = card;

			cardsInRound++;

			if (cardsInRound == 4)
				currentFirstCard = null;

			cardsInRound %= 4;
		}

		@Override public void readyForNewGame(PlayerSeat player) {}
		@Override public void throwCards(PlayerSeat player) {}

		@Override
		public void turn(PlayerSeat player)
		{
			if (player != seat)
				return;

			if (phase == PhaseEnum.CHANGING)
			{
				List<Card> cardsToSkart = myCards.filter(new SkartableCardFilter(gameType)).subList(0, myCards.size() - 9);
				enqueueAction(handler -> handler.change(seat, cardsToSkart));
			}
			else if (phase == PhaseEnum.GAMEPLAY)
			{
				Card cardToPlay = chooseRandom(myCards.getPlaceableCards(currentFirstCard));
				myCards.removeCard(cardToPlay);
				if (currentFirstCard == null)
				{
					enqueueActionExtraDelayed(handler -> handler.playCard(seat, cardToPlay));
				}
				else
				{
					enqueueActionDelayed(handler -> handler.playCard(seat, cardToPlay));
				}
			}
		}

		@Override public void startGame(PlayerSeat s, List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
		{
			seat = s;
			this.gameType = gameType;
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
			enqueueActionDelayed(handler -> handler.bid(seat, chooseRandom(bids)));
		}

		@Override
		public void availabeCalls(Collection<Card> cards)
		{
			enqueueActionDelayed(handler -> handler.call(seat, chooseRandom(cards)));
		}

		@Override public void changeDone(PlayerSeat player) {}
		@Override public void skartTarock(PlayerSeat.Map<Integer> counts) {}

		@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			if (announcements.contains(new AnnouncementContra(Announcements.hkp, 0)))
				enqueueActionDelayed(handler -> handler.announce(seat, new AnnouncementContra(Announcements.hkp, 0)));

			if (!announcements.isEmpty() && rnd.nextFloat() < 0.3)
			{
				enqueueActionDelayed(handler -> handler.announce(seat, chooseRandom(announcements)));
			}
			else
			{
				enqueueActionDelayed(handler -> handler.announcePassz(seat));
			}
		}

		@Override public void cardsTaken(PlayerSeat player) {}
		@Override public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points) {}

		@Override
		public void pendingNewGame()
		{
			enqueueAction(handler -> handler.readyForNewGame(seat));
		}
	}
}
