package com.tisza.tarock.net;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;

import java.util.*;
import java.util.stream.*;

import static com.tisza.tarock.net.Utils.*;
import static com.tisza.tarock.proto.EventProto.*;

public class ProtoEventSender implements EventSender
{
	private ProtoConnection connection;
	private boolean startEventSent = false;

	public void useConnection(ProtoConnection connection)
	{
		this.connection = connection;
		startEventSent = false;
	}

	private void sendEvent(Event event)
	{
		if (connection == null)
			return;

		if (!startEventSent)
			return;

		connection.sendMessage(MainProto.Message.newBuilder().setEvent(event).build());
	}

	private void sendPlayerActionEvent(PlayerSeat player, ActionProto.Action action)
	{
		Event.PlayerAction event = Event.PlayerAction.newBuilder()
				.setPlayer(player.asInt())
				.setAction(action)
				.build();
		sendEvent(Event.newBuilder().setPlayerAction(event).build());
	}

	@Override
	public void announce(PlayerSeat player, AnnouncementContra announcement)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setAnnounce(ActionProto.Action.Announce.newBuilder().setAnnouncement(Utils.announcementToProto(announcement))).build());
	}

	@Override
	public void announcePassz(PlayerSeat player)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setAnnoucePassz(ActionProto.Action.AnnouncePassz.newBuilder()).build());
	}

	@Override
	public void bid(PlayerSeat player, int bid)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setBid(ActionProto.Action.Bid.newBuilder().setBid(bid)).build());
	}

	@Override
	public void call(PlayerSeat player, Card card)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setCall(ActionProto.Action.Call.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	@Override
	public void playCard(PlayerSeat player, Card card)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setPlayCard(ActionProto.Action.PlayCard.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	@Override
	public void readyForNewGame(PlayerSeat player)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setReadyForNewGame(ActionProto.Action.ReadyForNewGame.newBuilder()).build());
	}

	@Override
	public void throwCards(PlayerSeat player)
	{
		sendPlayerActionEvent(player, ActionProto.Action.newBuilder().setThrowCards(ActionProto.Action.ThrowCards.newBuilder()).build());
	}

	@Override public void turn(PlayerSeat player)
	{
		Event.Turn e = Event.Turn.newBuilder()
				.setPlayer(player.asInt())
				.build();
		sendEvent(Event.newBuilder().setTurn(e).build());
	}

	@Override public void startGame(PlayerSeat seat, List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
	{
		startEventSent = true;

		Event.StartGame e = Event.StartGame.newBuilder()
				.setMyId(seat.asInt())
				.addAllPlayerName(names)
				.setGameType(Utils.gameTypeToProto(gameType))
				.setBeginnerPlayer(beginnerPlayer.asInt())
				.build();
		sendEvent(Event.newBuilder().setStartGame(e).build());
	}

	@Override public void playerCards(PlayerCards cards)
	{
		Event.PlayerCards e = Event.PlayerCards.newBuilder()
				.addAllCard(cards.getCards().stream().map(Utils::cardToProto).collect(Collectors.toList()))
				.build();
		sendEvent(Event.newBuilder().setPlayerCards(e).build());
	}

	@Override public void phaseChanged(PhaseEnum phase)
	{
		Event.PhaseChanged e = Event.PhaseChanged.newBuilder()
				.setPhase(phaseToProto(phase))
				.build();
		sendEvent(Event.newBuilder().setPhaseChanged(e).build());
	}

	@Override public void availabeBids(Collection<Integer> bids)
	{
		Event.AvailableBids e = Event.AvailableBids.newBuilder()
				.addAllBid(bids)
				.build();
		sendEvent(Event.newBuilder().setAvailableBids(e).build());
	}

	@Override public void availabeCalls(Collection<Card> cards)
	{
		Event.AvailableCalls e = Event.AvailableCalls.newBuilder()
				.addAllCard(cards.stream().map(Utils::cardToProto).collect(Collectors.toList()))
				.build();
		sendEvent(Event.newBuilder().setAvailableCalls(e).build());
	}

	@Override public void changeDone(PlayerSeat player)
	{
		Event.ChangeDone e = Event.ChangeDone.newBuilder()
				.setPlayer(player.asInt())
				.build();
		sendEvent(Event.newBuilder().setChangeDone(e).build());
	}

	@Override public void skartTarock(PlayerSeat.Map<Integer> counts)
	{
		Event.SkartTarock.Builder e = Event.SkartTarock.newBuilder();

		for (int count : counts)
		{
			e.addCount(count);
		}

		sendEvent(Event.newBuilder().setSkartTarock(e).build());
	}

	@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
	{
		Event.AvailableAnnouncements e = Event.AvailableAnnouncements.newBuilder()
				.addAllAnnouncement(announcements.stream().map(Utils::announcementToProto).collect(Collectors.toList()))
				.build();
		sendEvent(Event.newBuilder().setAvailableAnnouncements(e).build());
	}

	@Override public void cardsTaken(PlayerSeat player)
	{
		Event.CardsTaken e = Event.CardsTaken.newBuilder()
				.setPlayer(player.asInt())
				.build();
		sendEvent(Event.newBuilder().setCardsTaken(e).build());
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

		sendEvent(Event.newBuilder().setAnnouncementStatistics(e).build());
	}

	@Override public void pendingNewGame()
	{
		Event.PendingNewGame e = Event.PendingNewGame.newBuilder().build();
		sendEvent(Event.newBuilder().setPendingNewGame(e).build());
	}
}
