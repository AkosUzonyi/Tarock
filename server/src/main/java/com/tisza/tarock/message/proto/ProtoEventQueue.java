package com.tisza.tarock.message.proto;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.proto.*;

import java.util.*;
import java.util.stream.*;

import static com.tisza.tarock.message.proto.Utils.phaseToProto;
import static com.tisza.tarock.proto.EventProto.*;

public class ProtoEventQueue implements EventQueue
{
	private Connection connection;

	public ProtoEventQueue(Connection connection)
	{
		this.connection = connection;
	}

	private void processEvent(Event event)
	{
		connection.sendPacket(new PacketEvent(event));
	}

	private void processPlayerActionEvent(int player, ActionProto.Action action)
	{
		Event.PlayerAction event = Event.PlayerAction.newBuilder()
				.setPlayer(player)
				.setAction(action)
				.build();
		processEvent(Event.newBuilder().setPlayerAction(event).build());
	}

	@Override
	public void announce(int player, AnnouncementContra announcement)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setAnnounce(ActionProto.Action.Announce.newBuilder().setAnnouncement(Utils.announcementToProto(announcement))).build());
	}

	@Override
	public void announcePassz(int player)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setAnnoucePassz(ActionProto.Action.AnnouncePassz.newBuilder()).build());
	}

	@Override
	public void bid(int player, int bid)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setBid(ActionProto.Action.Bid.newBuilder().setBid(bid)).build());
	}

	@Override
	public void call(int player, Card card)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setCall(ActionProto.Action.Call.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	@Override
	public void playCard(int player, Card card)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setPlayCard(ActionProto.Action.PlayCard.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	@Override
	public void readyForNewGame(int player)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setReadyForNewGame(ActionProto.Action.ReadyForNewGame.newBuilder()).build());
	}

	@Override
	public void throwCards(int player)
	{
		processPlayerActionEvent(player, ActionProto.Action.newBuilder().setThrowCards(ActionProto.Action.ThrowCards.newBuilder()).build());
	}

	@Override public void turn(int player)
	{
		Event.Turn e = Event.Turn.newBuilder()
				.setPlayer(player)
				.build();
		processEvent(Event.newBuilder().setTurn(e).build());
	}

	@Override public void startGame(int id, List<String> names)
	{
		Event.StartGame e = Event.StartGame.newBuilder()
				.setMyId(id)
				.addAllPlayerName(names)
				.build();
		processEvent(Event.newBuilder().setStartGame(e).build());
	}

	@Override public void playerCards(PlayerCards cards)
	{
		Event.PlayerCards e = Event.PlayerCards.newBuilder()
				.addAllCard(cards.getCards().stream().map(Utils::cardToProto).collect(Collectors.toList()))
				.build();
		processEvent(Event.newBuilder().setPlayerCards(e).build());
	}

	@Override public void phaseChanged(PhaseEnum phase)
	{
		Event.PhaseChanged e = Event.PhaseChanged.newBuilder()
				.setPhase(phaseToProto(phase))
				.build();
		processEvent(Event.newBuilder().setPhaseChanged(e).build());
	}

	@Override public void availabeBids(Collection<Integer> bids)
	{
		Event.AvailableBids e = Event.AvailableBids.newBuilder()
				.addAllBid(bids)
				.build();
		processEvent(Event.newBuilder().setAvailableBids(e).build());
	}

	@Override public void availabeCalls(Collection<Card> cards)
	{
		Event.AvailableCalls e = Event.AvailableCalls.newBuilder()
				.addAllCard(cards.stream().map(Utils::cardToProto).collect(Collectors.toList()))
				.build();
		processEvent(Event.newBuilder().setAvailableCalls(e).build());
	}

	@Override public void cardsFromTalon(List<Card> cards)
	{
		Event.CardsFromTalon e = Event.CardsFromTalon.newBuilder()
				.addAllCard(cards.stream().map(Utils::cardToProto).collect(Collectors.toList()))
				.build();
		processEvent(Event.newBuilder().setCardsFromTalon(e).build());
	}

	@Override public void changeDone(int player)
	{
		Event.ChangeDone e = Event.ChangeDone.newBuilder()
				.setPlayer(player)
				.build();
		processEvent(Event.newBuilder().setChangeDone(e).build());
	}

	@Override public void skartTarock(int[] counts)
	{
		Event.SkartTarock.Builder e = Event.SkartTarock.newBuilder();

		for (int count : counts)
		{
			e.addCount(count);
		}

		processEvent(Event.newBuilder().setSkartTarock(e).build());
	}

	@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
	{
		Event.AvailableAnnouncements e = Event.AvailableAnnouncements.newBuilder()
				.addAllAnnouncement(announcements.stream().map(Utils::announcementToProto).collect(Collectors.toList()))
				.build();
		processEvent(Event.newBuilder().setAvailableAnnouncements(e).build());
	}

	@Override public void cardsTaken(int player)
	{
		Event.CardsTaken e = Event.CardsTaken.newBuilder()
				.setPlayer(player)
				.build();
		processEvent(Event.newBuilder().setCardsTaken(e).build());
	}

	@Override public void announcementStatistics(int selfGamePoints, int opponentGamePoints, List<AnnouncementStaticticsEntry> selfEntries, List<AnnouncementStaticticsEntry> opponentEntries, int sumPoints, int[] points)
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

		processEvent(Event.newBuilder().setAnnouncementStatistics(e).build());
	}

	@Override public void pendingNewGame()
	{
		Event.PendingNewGame e = Event.PendingNewGame.newBuilder().build();
		processEvent(Event.newBuilder().setPendingNewGame(e).build());
	}
}
