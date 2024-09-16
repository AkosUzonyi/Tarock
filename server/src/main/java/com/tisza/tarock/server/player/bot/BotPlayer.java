package com.tisza.tarock.server.player.bot;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.server.player.*;

import java.util.*;

public class BotPlayer extends NonHumanPlayer
{

	private final Brain brain;

	public BotPlayer(User user, int delay, int extraDelay)
	{
		super(user, delay, extraDelay);
		brain = new Brain(new Personality());
		setEventHandler(new SmartBotHandler());
	}

	private class SmartBotHandler implements EventHandler
	{
		private PhaseEnum phase;
		private boolean isMyTurn;
		private int cardsInTrick = 0;

		@Override
		public void historyMode(boolean isHistory)
		{
			historyMode = isHistory;

			if (!historyMode && lastActionInHistoryMode != null && isMyTurn)
				enqueueActionDelayed(lastActionInHistoryMode, 0);

			lastActionInHistoryMode = null;
		}

		@Override
		public void announce(PlayerSeat player, AnnouncementContra announcement)
		{
			brain.announce(player, announcement);
		}

		@Override
		public void announcePassz(PlayerSeat player)
		{
		}

		@Override
		public void bid(PlayerSeat player, int bid)
		{
			brain.bid(player, bid);
		}

		@Override
		public void call(PlayerSeat player, Card card)
		{
			brain.call(player, card);
		}

		@Override
		public void playCard(PlayerSeat player, Card card)
		{
			brain.playCard(player, card, player == getSeat());
			cardsInTrick++;
			cardsInTrick %= 4;
		}

		@Override
		public void readyForNewGame(PlayerSeat player)
		{
		}

		@Override
		public void throwCards(PlayerSeat player)
		{
		}

		@Override
		public void turn(PlayerSeat player)
		{
			isMyTurn = player == getSeat();

			if (!isMyTurn)
				return;

			if (phase == PhaseEnum.CHANGING)
			{
				enqueueActionDelayed(Action.fold(brain.fold()), 0);
			}
			else if (phase == PhaseEnum.GAMEPLAY)
			{
				if (cardsInTrick == 0)
				{
					enqueueActionDelayed(Action.play(brain.turn()), extraDelay);
				}
				else
				{
					enqueueActionDelayed(Action.play(brain.turn()));
				}
			}
		}

		@Override
		public void startGame(GameType gameType, int beginnerPlayer)
		{
			brain.startGame(gameType, beginnerPlayer);
		}

		@Override
		public void playerCards(PlayerCards cards, boolean canBeThrown)
		{
			if (brain.playerCards(cards, canBeThrown))
			{
				enqueueActionDelayed(Action.throwCards());
			}
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
			enqueueActionDelayed(Action.bid(brain.availableBids(bids)));
		}

		@Override
		public void availableCalls(Collection<Card> cards)
		{
			enqueueActionDelayed(Action.call(brain.availableCalls(cards)));
		}

		@Override
		public void foldDone(PlayerSeat player)
		{
		}

		@Override
		public void foldTarock(PlayerSeatMap<Integer> counts)
		{
			brain.foldTarock(counts);
		}

		@Override
		public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			for (AnnouncementContra announcement : brain.availableAnnouncements(announcements))
			{
				enqueueActionDelayed(Action.announce(announcement));
			}
			enqueueActionDelayed(Action.announcePassz());
		}

		@Override
		public void cardsTaken(PlayerSeat player)
		{
		}

		@Override
		public void announcementStatistics(int selfGamePoints, int opponentGamePoints,
				List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
		{
		}

		@Override
		public void pendingNewGame()
		{
			enqueueActionDelayed(Action.readyForNewGame(), 0);
		}
	}

	private void enqueueActionDelayed(Action action)
	{
		enqueueActionDelayed(action, delay);
	}
}
