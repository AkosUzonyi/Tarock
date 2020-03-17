package com.tisza.tarock.server.player;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.server.*;
import com.tisza.tarock.server.database.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.server.net.*;
import com.tisza.tarock.proto.*;

import java.util.*;
import java.util.stream.*;

public class ProtoPlayer extends Player implements MessageHandler
{
	private ProtoConnection connection;

	public ProtoPlayer(User user, String name)
	{
		super(user, name);
	}

	public void useConnection(ProtoConnection connection)
	{
		if (this.connection != null)
			this.connection.removeMessageHandler(this);

		this.connection = connection;

		if (connection != null)
		{
			connection.addMessageHandler(this);
			requestHistory();
		}
	}
	@Override
	public void handleEvent(Event event)
	{
		event.handle(eventHandler);
	}

	@Override
	public void handleMessage(MainProto.Message message)
	{
		if (message.getMessageTypeCase() == MainProto.Message.MessageTypeCase.ACTION)
			doAction(new Action(message.getAction()));
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
		public void chat(int userID, String message)
		{
			EventProto.Event.Chat e = EventProto.Event.Chat.newBuilder()
					.setUserId(userID)
					.setMessage(message)
					.build();
			sendEvent(EventProto.Event.newBuilder().setChat(e).build());
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

		@Override public void startGame(GameType gameType, PlayerSeat beginnerPlayer)
		{
			EventProto.Event.StartGame e = EventProto.Event.StartGame.newBuilder()
					.setGameType(gameType.getID())
					.setBeginnerPlayer(beginnerPlayer.asInt())
					.build();
			sendEvent(EventProto.Event.newBuilder().setStartGame(e).build());
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

		@Override public void availableBids(Collection<Integer> bids)
		{
			EventProto.Event.AvailableBids e = EventProto.Event.AvailableBids.newBuilder()
					.addAllBid(bids)
					.build();
			sendEvent(EventProto.Event.newBuilder().setAvailableBids(e).build());
		}

		@Override public void availableCalls(Collection<Card> cards)
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
	};
}
