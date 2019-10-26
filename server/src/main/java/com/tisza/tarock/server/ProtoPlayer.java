package com.tisza.tarock.server;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.net.*;
import com.tisza.tarock.proto.*;
import io.reactivex.*;

import java.util.*;
import java.util.stream.*;

public class ProtoPlayer implements Player, MessageHandler
{
	private User user;
	private String name;
	private ProtoConnection connection;
	private Game game;
	private PlayerSeat seat;

	private ProtoPlayer(User user, String name)
	{
		this.user = user;
		this.name = name;
	}

	public static Single<ProtoPlayer> createFromUser(User user)
	{
		return user.getName().map(name -> new ProtoPlayer(user, name));
	}

	public User getUser()
	{
		return user;
	}

	public void useConnection(ProtoConnection connection)
	{
		if (this.connection != null)
			this.connection.removeMessageHandler(this);

		this.connection = connection;

		if (connection != null)
		{
			connection.addMessageHandler(this);
			if (game != null)
				game.requestHistory(seat, eventHandler);
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void handleEvent(Event event)
	{
		event.handle(eventHandler);
	}

	@Override
	public void setGame(Game game, PlayerSeat seat)
	{
		this.game = game;
		this.seat = seat;
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		if (seat != null && message.getMessageTypeCase() == MainProto.Message.MessageTypeCase.ACTION)
			game.action(seat, new Action(message.getAction()));
	}

	@Override
	public void connectionClosed()
	{
		connection = null;
	}

	private final EventHandler eventHandler = new EventHandler()
	{
		private void sendEvent(EventProto.Event event)
		{
			if (connection == null)
				return;

			connection.sendMessage(MainProto.Message.newBuilder().setEvent(event).build());
		}

		private void sendPlayerActionEvent(PlayerSeat player, Action action)
		{
			EventProto.Event.PlayerAction event = EventProto.Event.PlayerAction.newBuilder()
					.setPlayer(player.asInt())
					.setAction(action.getId())
					.build();
			sendEvent(EventProto.Event.newBuilder().setPlayerAction(event).build());
		}

		@Override
		public void announce(PlayerSeat player, AnnouncementContra announcement)
		{
			sendPlayerActionEvent(player, Action.announce(announcement));
		}

		@Override
		public void announcePassz(PlayerSeat player)
		{
			sendPlayerActionEvent(player, Action.announcePassz());
		}

		@Override
		public void bid(PlayerSeat player, int bid)
		{
			sendPlayerActionEvent(player, Action.bid(bid));
		}

		@Override
		public void call(PlayerSeat player, Card card)
		{
			sendPlayerActionEvent(player, Action.call(card));
		}

		@Override
		public void playCard(PlayerSeat player, Card card)
		{
			sendPlayerActionEvent(player, Action.play(card));
		}

		@Override
		public void readyForNewGame(PlayerSeat player)
		{
			sendPlayerActionEvent(player, Action.readyForNewGame());
		}

		@Override
		public void throwCards(PlayerSeat player)
		{
			sendPlayerActionEvent(player, Action.throwCards());
		}

		@Override
		public void chat(PlayerSeat player, String message)
		{
			sendPlayerActionEvent(player, Action.chat(message));
		}

		@Override public void turn(PlayerSeat player)
		{
			EventProto.Event.Turn e = EventProto.Event.Turn.newBuilder()
					.setPlayer(player.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setTurn(e).build());
		}

		@Override public void playerTeamInfo(PlayerSeat player, Team team)
		{
			EventProto.Event.PlayerTeamInfo e = EventProto.Event.PlayerTeamInfo.newBuilder()
					.setPlayer(player.asInt())
					.setIsCaller(team == Team.CALLER)
					.build();
			sendEvent(EventProto.Event.newBuilder().setPlayerTeamInfo(e).build());
		}

		@Override public void startGame(List<String> names, GameType gameType, PlayerSeat beginnerPlayer)
		{
			EventProto.Event.StartGame e = EventProto.Event.StartGame.newBuilder()
					.addAllPlayerName(names)
					.setGameType(gameType.getID())
					.setBeginnerPlayer(beginnerPlayer.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setStartGame(e).build());
		}

		@Override
		public void seat(PlayerSeat seat)
		{
			EventProto.Event.Seat e = EventProto.Event.Seat.newBuilder()
					.setSeat(seat == null ? -1 : seat.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setSeat(e).build());
		}

		@Override public void playerCards(PlayerCards cards)
		{
			EventProto.Event.PlayerCards e = EventProto.Event.PlayerCards.newBuilder()
					.addAllCard(cards.getCards().stream().map(Card::getID).collect(Collectors.toList()))
					.setCanBeThrown(cards.canBeThrown())
					.build();
			sendEvent(EventProto.Event.newBuilder().setPlayerCards(e).build());
		}

		@Override public void phaseChanged(PhaseEnum phase)
		{
			EventProto.Event.PhaseChanged e = EventProto.Event.PhaseChanged.newBuilder()
					.setPhase(phase.getID())
					.build();
			sendEvent(EventProto.Event.newBuilder().setPhaseChanged(e).build());
		}

		@Override public void availabeBids(Collection<Integer> bids)
		{
			EventProto.Event.AvailableBids e = EventProto.Event.AvailableBids.newBuilder()
					.addAllBid(bids)
					.build();
			sendEvent(EventProto.Event.newBuilder().setAvailableBids(e).build());
		}

		@Override public void availabeCalls(Collection<Card> cards)
		{
			EventProto.Event.AvailableCalls e = EventProto.Event.AvailableCalls.newBuilder()
					.addAllCard(cards.stream().map(Card::getID).collect(Collectors.toList()))
					.build();
			sendEvent(EventProto.Event.newBuilder().setAvailableCalls(e).build());
		}

		@Override public void changeDone(PlayerSeat player)
		{
			EventProto.Event.ChangeDone e = EventProto.Event.ChangeDone.newBuilder()
					.setPlayer(player.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setChangeDone(e).build());
		}

		@Override public void skartTarock(PlayerSeatMap<Integer> counts)
		{
			EventProto.Event.SkartTarock.Builder e = EventProto.Event.SkartTarock.newBuilder();

			for (int count : counts)
			{
				e.addCount(count);
			}

			sendEvent(EventProto.Event.newBuilder().setSkartTarock(e).build());
		}

		@Override public void availableAnnouncements(List<AnnouncementContra> announcements)
		{
			EventProto.Event.AvailableAnnouncements e = EventProto.Event.AvailableAnnouncements.newBuilder()
					.addAllAnnouncement(announcements.stream().map(AnnouncementContra::getID).collect(Collectors.toList()))
					.build();
			sendEvent(EventProto.Event.newBuilder().setAvailableAnnouncements(e).build());
		}

		@Override public void cardsTaken(PlayerSeat player)
		{
			EventProto.Event.CardsTaken e = EventProto.Event.CardsTaken.newBuilder()
					.setPlayer(player.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setCardsTaken(e).build());
		}

		@Override public void announcementStatistics(int callerGamePoints, int opponentGamePoints, List<AnnouncementResult> announcementResults, int sumPoints, int pointMultiplier)
		{
			EventProto.Event.Statistics.Builder e = EventProto.Event.Statistics.newBuilder()
					.setCallerGamePoints(callerGamePoints)
					.setOpponentGamePoints(opponentGamePoints)
					.addAllAnnouncementResult(announcementResults.stream().map(Utils::announcementResultToProto).collect(Collectors.toList()))
					.setSumPoints(sumPoints)
					.setPointMultiplier(pointMultiplier);

			sendEvent(EventProto.Event.newBuilder().setStatistics(e).build());
		}

		@Override
		public void playerPoints(int[] points)
		{
			EventProto.Event.PlayerPoints.Builder e = EventProto.Event.PlayerPoints.newBuilder();

			for (int point : points)
			{
				e.addPlayerPoint(point);
			}

			sendEvent(EventProto.Event.newBuilder().setPlayerPoints(e).build());
		}

		@Override public void pendingNewGame()
		{
			EventProto.Event.PendingNewGame e = EventProto.Event.PendingNewGame.newBuilder().build();
			sendEvent(EventProto.Event.newBuilder().setPendingNewGame(e).build());
		}

		@Override public void deleteGame()
		{
			EventProto.Event.DeleteGame e = EventProto.Event.DeleteGame.newBuilder().build();
			sendEvent(EventProto.Event.newBuilder().setDeleteGame(e).build());
		}
	};
}
