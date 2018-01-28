package com.tisza.tarock.message;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.game.AnnouncementContra;
import com.tisza.tarock.game.PhaseEnum;
import com.tisza.tarock.proto.ActionProto.Action;
import com.tisza.tarock.proto.EventProto.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.tisza.tarock.message.Utils.phaseToProto;

public class GameEventQueue
{
	private List<List<Event>> eventsForPlayers = new ArrayList<>();

	private Collector broadcastCollector;
	private Collector[] playerCollectors = new Collector[4];

	public GameEventQueue()
	{
		broadcastCollector = new BroadcastCollector();
		for (int i = 0; i < 4; i++)
		{
			eventsForPlayers.add(new ArrayList<>());
			playerCollectors[i] = new PlayerCollector(i);
		}
	}

	public List<Event> pollEventsForPlayer(int player)
	{
		List<Event> result = new ArrayList<>(eventsForPlayers.get(player));
		eventsForPlayers.get(player).clear();
		return result;
	}

	public Collector toPlayer(int player)
	{
		return playerCollectors[player];
	}

	public Collector broadcast()
	{
		return broadcastCollector;
	}

	private void eventForPlayer(int player, Event event)
	{
		eventsForPlayers.get(player).add(event);
	}

	private class PlayerCollector extends Collector
	{
		private int player;

		public PlayerCollector(int player)
		{
			this.player = player;
		}

		protected void collectEvent(Event event)
		{
			eventForPlayer(player, event);
		}
	}

	private class BroadcastCollector extends Collector
	{
		protected void collectEvent(Event event)
		{
			for (int i = 0; i < 4; i++)
			{
				eventForPlayer(i, event);
			}
		}
	}

	public static abstract class Collector
	{
		protected abstract void collectEvent(Event event);

		public void playerAction(int player, Action action)
		{
			Event.PlayerAction e = Event.PlayerAction.newBuilder()
					.setPlayer(player)
					.setAction(action)
					.build();
			collectEvent(Event.newBuilder().setPlayerAction(e).build());
		}

		public void turn(int player)
		{
			Event.Turn e = Event.Turn.newBuilder()
					.setPlayer(player)
					.build();
			collectEvent(Event.newBuilder().setTurn(e).build());
		}

		public void startGame(int id, List<String> names)
		{
			Event.StartGame e = Event.StartGame.newBuilder()
					.setMyId(id)
					.addAllPlayerName(names)
					.build();
			collectEvent(Event.newBuilder().setStartGame(e).build());
		}

		public void playerCards(PlayerCards cards)
		{
			Event.PlayerCards e = Event.PlayerCards.newBuilder()
					.addAllCard(cards.getCards().stream().map(Utils::cardToProto).collect(Collectors.toList()))
					.build();
			collectEvent(Event.newBuilder().setPlayerCards(e).build());
		}

		public void phaseChanged(PhaseEnum phase)
		{
			Event.PhaseChanged e = Event.PhaseChanged.newBuilder()
					.setPhase(phaseToProto(phase))
					.build();
			collectEvent(Event.newBuilder().setPhaseChanged(e).build());
		}

		public void availabeBids(Collection<Integer> bids)
		{
			Event.AvailableBids e = Event.AvailableBids.newBuilder()
					.addAllBid(bids)
					.build();
			collectEvent(Event.newBuilder().setAvailableBids(e).build());
		}

		public void availabeCalls(Collection<Card> cards)
		{
			Event.AvailableCalls e = Event.AvailableCalls.newBuilder()
					.addAllCard(cards.stream().map(Utils::cardToProto).collect(Collectors.toList()))
					.build();
			collectEvent(Event.newBuilder().setAvailableCalls(e).build());
		}

		public void cardsFromTalon(List<Card> cards)
		{
			Event.CardsFromTalon e = Event.CardsFromTalon.newBuilder()
					.addAllCard(cards.stream().map(Utils::cardToProto).collect(Collectors.toList()))
					.build();
			collectEvent(Event.newBuilder().setCardsFromTalon(e).build());
		}

		public void changeDone(int player)
		{
			Event.ChangeDone e = Event.ChangeDone.newBuilder()
					.setPlayer(player)
					.build();
			collectEvent(Event.newBuilder().setChangeDone(e).build());
		}

		public void skartTarock(int[] counts)
		{
			Event.SkartTarock.Builder e = Event.SkartTarock.newBuilder();

			for (int count : counts)
			{
				e.addCount(count);
			}

			collectEvent(Event.newBuilder().setSkartTarock(e).build());
		}

		public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			Event.AvailableAnnouncements e = Event.AvailableAnnouncements.newBuilder()
					.addAllAnnouncement(announcements.stream().map(Utils::announcementToProto).collect(Collectors.toList()))
					.build();
			collectEvent(Event.newBuilder().setAvailableAnnouncements(e).build());
		}

		public void cardsTaken(int player)
		{
			Event.CardsTaken e = Event.CardsTaken.newBuilder()
					.setPlayer(player)
					.build();
			collectEvent(Event.newBuilder().setCardsTaken(e).build());
		}

		public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points)
		{
			Event.AnnouncementStatistics.Builder e = Event.AnnouncementStatistics.newBuilder()
					.setSelfGamePoints(selfGamePoints)
					.setOpponentGamePoints(opponentGamePoints)
					.addAllSelfEntry(selfEntries.stream().map(Utils::statisticsEntryToProto).collect(Collectors.toList()))
					.addAllOpponentEntry(opponentEntries.stream().map(Utils::statisticsEntryToProto).collect(Collectors.toList()))
					.setSumPoints(sumPoints);

			for (int point : points)
			{
				e.addPlayerPoint(point);
			}

			collectEvent(Event.newBuilder().setAnnouncementStatistics(e).build());
		}

		public void pendingNewGame()
		{
			Event.PendingNewGame e = Event.PendingNewGame.newBuilder().build();
			collectEvent(Event.newBuilder().setPendingNewGame(e).build());
		}
	}
}
