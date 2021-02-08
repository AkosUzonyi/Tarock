package com.tisza.tarock.server.player;

import com.tisza.tarock.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.concurrent.*;

public class RandomPlayer extends Player
{
	private final int delay, extraDelay;
	private EventHandler eventHandler = new MyEventHandler();
	private Random rnd = new Random();

	private boolean historyMode;
	private Action lastActionInHistoryMode;
	private boolean isMyTurn;

	public RandomPlayer(User user)
	{
		this(user, 0, 0);
	}

	public RandomPlayer(User user, int delay, int extraDelay)
	{
		super(user);
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
		if (historyMode)
		{
			lastActionInHistoryMode = action;
			return;
		}

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

		@Override
		public void historyMode(boolean isHistory)
		{
			historyMode = isHistory;

			if (!historyMode && lastActionInHistoryMode != null && isMyTurn)
				enqueueActionDelayed(lastActionInHistoryMode, 0);

			lastActionInHistoryMode = null;
		}

		@Override public void announce(PlayerSeat player, AnnouncementContra announcement) {}
		@Override public void announcePassz(PlayerSeat player) {}
		@Override public void bid(PlayerSeat player, int bid) {}
		@Override public void call(PlayerSeat player, Card card) {}

		private Card currentFirstCard = null;
		private int cardsInTrick;

		@Override
		public void playCard(PlayerSeat player, Card card)
		{
			if (player == getSeat())
				myCards.removeCard(card);

			if (cardsInTrick == 0)
				currentFirstCard = card;

			cardsInTrick++;

			if (cardsInTrick == 4)
				currentFirstCard = null;

			cardsInTrick %= 4;
		}

		@Override public void readyForNewGame(PlayerSeat player) {}
		@Override public void throwCards(PlayerSeat player) {}

		@Override
		public void turn(PlayerSeat player)
		{
			isMyTurn = player == getSeat();
			if (!isMyTurn)
				return;

			if (phase == PhaseEnum.CHANGING)
			{
				List<Card> cardsToSkart = myCards.filter(new SkartableCardFilter(gameType)).subList(0, myCards.size() - 9);
				enqueueActionDelayed(Action.skart(cardsToSkart), 0);
			}
			else if (phase == PhaseEnum.GAMEPLAY)
			{
				Card cardToPlay = chooseRandom(myCards.getPlayableCards(currentFirstCard));
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

		@Override public void startGame(GameType gameType, int beginnerPlayer)
		{
			this.gameType = gameType;
		}

		@Override
		public void playerCards(PlayerCards cards, boolean canBeThrown)
		{
			myCards = cards.clone();
		}

		@Override
		public void phaseChanged(PhaseEnum phase)
		{
			this.phase = phase;
			isMyTurn = false;
		}

		@Override
		public void availableBids(Collection<Integer> bids)
		{
			enqueueActionDelayed(Action.bid(chooseRandom(bids)), delay);
		}

		@Override
		public void availableCalls(Collection<Card> cards)
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
