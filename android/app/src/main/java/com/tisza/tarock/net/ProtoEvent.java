package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class ProtoEvent implements Event
{
	private final EventProto.Event event;

	public ProtoEvent(EventProto.Event event)
	{
		this.event = event;
	}

	private static List<Card> cardIDsToCards(List<String> cardIDs)
	{
		List<Card> cards = new ArrayList<>();
		for (String cardID : cardIDs)
		{
			cards.add(Card.fromId(cardID));
		}
		return cards;
	}

	@Override
	public void handle(EventHandler handler)
	{
		switch (event.getEventTypeCase())
		{
			case START_GAME:
				EventProto.Event.StartGame startGame = event.getStartGame();
				handler.startGame(startGame.getMyId(), startGame.getPlayerNameList(), GameType.fromID(startGame.getGameType()), startGame.getBeginnerPlayer());
				break;
			case TURN:
				handler.turn(event.getTurn().getPlayer());
				break;
			case PLAYER_TEAM_INFO:
				EventProto.Event.PlayerTeamInfo playerTeamInfo = event.getPlayerTeamInfo();
				handler.playerTeamInfo(playerTeamInfo.getPlayer(), playerTeamInfo.getIsCaller() ? Team.CALLER : Team.OPPONENT);
				break;
			case PLAYER_CARDS:
				handler.cardsChanged(cardIDsToCards(event.getPlayerCards().getCardList()), event.getPlayerCards().getCanBeThrown());
				break;
			case PHASE_CHANGED:
				handler.phaseChanged(PhaseEnum.fromID(event.getPhaseChanged().getPhase()));
				break;
			case AVAILABLE_BIDS:
				handler.availableBids(event.getAvailableBids().getBidList());
				break;
			case AVAILABLE_CALLS:
				handler.availableCalls(cardIDsToCards(event.getAvailableCalls().getCardList()));
				break;
			case CHANGE_DONE:
				handler.changeDone(event.getChangeDone().getPlayer());
				break;
			case SKART_TAROCK:
				int[] tarockCounts = new int[4];
				for (int i = 0; i < 4; i++)
				{
					tarockCounts[i] = event.getSkartTarock().getCount(i);
				}
				handler.skartTarock(tarockCounts);
				break;
			case AVAILABLE_ANNOUNCEMENTS:
				handler.availableAnnouncements(Utils.announcementListFromProto(event.getAvailableAnnouncements().getAnnouncementList()));
				break;
			case CARDS_TAKEN:
				handler.cardsTaken(event.getCardsTaken().getPlayer());
				break;
			case STATISTICS:
				EventProto.Event.Statistics statisticsEvent = event.getStatistics();
				int callerGamePoints = statisticsEvent.getCallerGamePoints();
				int opponentGamePoints = statisticsEvent.getOpponentGamePoints();
				List<AnnouncementResult> announcementResults = Utils.staticticsListFromProto(statisticsEvent.getAnnouncementResultList());
				int sumPoints = statisticsEvent.getSumPoints();
				int pointsMultiplier = statisticsEvent.getPointMultiplier();
				handler.statistics(callerGamePoints, opponentGamePoints, announcementResults, sumPoints, pointsMultiplier);
				break;
			case PLAYER_POINTS:
				EventProto.Event.PlayerPoints playerPointsEvent = event.getPlayerPoints();
				List<Integer> playerPoints = playerPointsEvent.getPlayerPointList();
				handler.playerPoints(playerPoints);
				break;
			case PENDING_NEW_GAME:
				handler.pendingNewGame();
				break;
			case DELETE_GAME:
				handler.deleteGame();
				break;
			case PLAYER_ACTION:
				handlePlayerAction(handler, event.getPlayerAction().getPlayer(), event.getPlayerAction().getAction());
				break;
			default:
				System.err.println("unhandled event");
				break;
		}
	}

	private void handlePlayerAction(EventHandler handler, int player, ActionProto.Action action)
	{
		switch (action.getActionTypeCase())
		{
			case BID:
				handler.bid(player, action.getBid().getBid());
				break;
			case CALL:
				handler.call(player, Card.fromId(action.getCall().getCard()));
				break;
			case CHANGE:
				System.err.println("change action should not be broadcasted");
				break;
			case ANNOUNCE:
				handler.announce(player, Announcement.fromID(action.getAnnounce().getAnnouncement()));
				break;
			case ANNOUCE_PASSZ:
				handler.announcePassz(player);
				break;
			case PLAY_CARD:
				handler.cardPlayed(player, Card.fromId(action.getPlayCard().getCard()));
				break;
			case THROW_CARDS:
				handler.cardsThrown(player, null);
				break;
			case CHAT:
				handler.chat(player, action.getChat().getMessage());
				break;
			case READY_FOR_NEW_GAME:
				handler.readyForNewGame(player);
				break;
			default:
				System.err.println("unhandled event");
				break;
		}
	}
}
