package com.tisza.tarock.net;

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

	public void handle(EventHandler handler)
	{
		switch (event.getEventTypeCase())
		{
			case START_GAME:
				handler.startGame(event.getStartGame().getMyId(), event.getStartGame().getPlayerNameList());
				break;
			case TURN:
				handler.turn(event.getTurn().getPlayer());
				break;
			case PLAYER_CARDS:
				handler.cardsChanged(Utils.cardListFromProto(event.getPlayerCards().getCardList()));
				break;
			case PHASE_CHANGED:
				handler.phaseChanged(Utils.phaseFromProto(event.getPhaseChanged().getPhase()));
				break;
			case AVAILABLE_BIDS:
				handler.availableBids(event.getAvailableBids().getBidList());
				break;
			case AVAILABLE_CALLS:
				handler.availableCalls(Utils.cardListFromProto(event.getAvailableCalls().getCardList()));
				break;
			case CARDS_FROM_TALON:
				handler.cardsFromTalon(Utils.cardListFromProto(event.getCardsFromTalon().getCardList()));
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
			case ANNOUNCEMENT_STATISTICS:
				EventProto.Event.AnnouncementStatistics statisticsEvent = event.getAnnouncementStatistics();
				int selfGamePoints = statisticsEvent.getSelfGamePoints();
				int opponentGamePoints = statisticsEvent.getOpponentGamePoints();
				List<AnnouncementStaticticsEntry> selfStatisticsEntries = Utils.staticticsListFromProto(statisticsEvent.getSelfEntryList());
				List<AnnouncementStaticticsEntry> opponentStatisticsEntries = Utils.staticticsListFromProto(statisticsEvent.getOpponentEntryList());
				int sumPoints = statisticsEvent.getSumPoints();
				List<Integer> playerPoints = statisticsEvent.getPlayerPointList();
				handler.statistics(selfGamePoints, opponentGamePoints, selfStatisticsEntries, opponentStatisticsEntries, sumPoints, playerPoints);
			case PENDING_NEW_GAME:
				handler.pendingNewGame();
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
				handler.call(player, Utils.cardFromProto(action.getCall().getCard()));
				break;
			case CHANGE:
				System.err.println("change action should not be broadcasted");
				break;
			case ANNOUNCE:
				handler.announce(player, Utils.announcementFromProto(action.getAnnounce().getAnnouncement()));
				break;
			case PLAY_CARD:
				handler.cardPlayed(player, Utils.cardFromProto(action.getPlayCard().getCard()));
				break;
			case THROW_CARDS:
				handler.cardsThrown(player, null);
				break;
			default:
				System.err.println("unhandled event");
				break;
		}
	}
}
