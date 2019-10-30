package com.tisza.tarock.server;

import com.tisza.tarock.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.concurrent.*;

public class RandomPlayer extends Player
{
	private final int delay, extraDelay;
	private EventHandler eventHandler = new MyEventHandler();
	private Random rnd = new Random();

	public RandomPlayer(User user, String name)
	{
		this(user, name, 0, 0);
	}

	public RandomPlayer(User user, String name, int delay, int extraDelay)
	{
		super(user, name);
		this.delay = delay;
		this.extraDelay = extraDelay;
	}

	@Override
	public void handleEvent(Event event)
	{
		event.handle(eventHandler);
	}

	private void enqueueActionDelayed(Action action, int delayMillis)
	{
		Main.GAME_EXECUTOR_SERVICE.schedule(() -> doAction(action), delayMillis, TimeUnit.MILLISECONDS);
	}

	private class MyEventHandler implements EventHandler
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
			if (player != getSeat())
				return;

			if (phase == PhaseEnum.CHANGING)
			{
				List<Card> cardsToSkart = myCards.filter(new SkartableCardFilter(gameType)).subList(0, myCards.size() - 9);
				enqueueActionDelayed(Action.skart(cardsToSkart), 0);
			}
			else if (phase == PhaseEnum.GAMEPLAY)
			{
				Card cardToPlay = chooseRandom(myCards.getPlaceableCards(currentFirstCard));
				myCards.removeCard(cardToPlay);
				if (currentFirstCard == null)
				{
					enqueueActionDelayed(Action.play(cardToPlay), extraDelay);
				}
				else
				{
					enqueueActionDelayed(Action.play(cardToPlay), delay);
				}
			}
		}

		@Override public void startGame(List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
		{
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
			enqueueActionDelayed(Action.bid(chooseRandom(bids)), delay);
		}

		@Override
		public void availabeCalls(Collection<Card> cards)
		{
			enqueueActionDelayed(Action.call(chooseRandom(cards)), delay);
		}

		@Override public void changeDone(PlayerSeat player) {}
		@Override public void skartTarock(PlayerSeatMap<Integer> counts) {}

		@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			if (announcements.contains(new AnnouncementContra(Announcements.hkp, 0)))
				enqueueActionDelayed(Action.announce(new AnnouncementContra(Announcements.hkp, 0)), delay);

			if (!announcements.isEmpty() && rnd.nextFloat() < 0.3)
			{
				enqueueActionDelayed(Action.announce(chooseRandom(announcements)), delay);
			}
			else
			{
				enqueueActionDelayed(Action.announcePassz(), delay);
			}
		}

		@Override public void cardsTaken(PlayerSeat player) {}
		@Override public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier) {}

		@Override
		public void pendingNewGame()
		{
			enqueueActionDelayed(Action.readyForNewGame(), 0);
		}
	}
}
