package com.tisza.tarock;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.phase.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.server.net.*;

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
				handler.startGame(GameType.fromID(startGame.getGameType()), PlayerSeat.fromInt(startGame.getBeginnerPlayer()));
				break;
			case TURN:
				handler.turn(PlayerSeat.fromInt(event.getTurn().getPlayer()));
				break;
			case PLAYER_TEAM_INFO:
				EventProto.Event.PlayerTeamInfo playerTeamInfo = event.getPlayerTeamInfo();
				handler.playerTeamInfo(PlayerSeat.fromInt(playerTeamInfo.getPlayer()), playerTeamInfo.getIsCaller() ? Team.CALLER : Team.OPPONENT);
				break;
			case PLAYER_CARDS:
				handler.playerCards(new PlayerCards(cardIDsToCards(event.getPlayerCards().getCardList())));
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
				handler.changeDone(PlayerSeat.fromInt(event.getChangeDone().getPlayer()));
				break;
			case AVAILABLE_ANNOUNCEMENTS:
				handler.availableAnnouncements(Utils.announcementListFromProto(event.getAvailableAnnouncements().getAnnouncementList()));
				break;
			case PENDING_NEW_GAME:
				handler.pendingNewGame();
				break;
			case PLAYER_ACTION:
				handlePlayerAction(handler, event.getPlayerAction().getPlayer(), event.getPlayerAction().getAction());
				break;
			case STATISTICS: case CARDS_TAKEN: case SKART_TAROCK: case PLAYER_POINTS:
				break;
			default:
				System.err.println("unhandled event");
				break;
		}
	}

	private void handlePlayerAction(EventHandler handler, int player, String action)
	{
		new Action(action).handle(PlayerSeat.fromInt(player), handler);
	}
}
