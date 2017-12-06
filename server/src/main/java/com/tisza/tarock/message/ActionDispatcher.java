package com.tisza.tarock.message;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;
import com.tisza.tarock.proto.ActionProto.*;

import java.util.stream.*;

public class ActionDispatcher
{
	private final ActionHandler actionHandler;

	public ActionDispatcher(ActionHandler actionHandler)
	{
		this.actionHandler = actionHandler;
	}

	public void dispatchAction(int player, Action action)
	{
		switch (action.getActionTypeCase())
		{
			case BID:
				actionHandler.bid(player, action.getBid().getBid());
				break;
			case CHANGE:
				actionHandler.change(player, action.getChange().getCardList().stream().map(this::createCardFromProto).collect(Collectors.toList()));
				break;
			case CALL:
				actionHandler.call(player, createCardFromProto(action.getCall().getCard()));
				break;
			case ANNOUNCE:
				actionHandler.announce(player, createAnnouncementFromProto(action.getAnnounce().getAnnoncement()));
				break;
			case ANNOUCE_PASSZ:
				actionHandler.announcePassz(player);
				break;
			case PLAY_CARD:
				actionHandler.playCard(player, createCardFromProto(action.getPlayCard().getCard()));
				break;
			case READY_FOR_NEW_GAME:
				actionHandler.readyForNewGame(player);
				break;
			default: System.err.println("unkown action: " + action.getActionTypeCase());
		}
	}

	private Card createCardFromProto(ProtoUtils.Card cardProto)
	{
		switch (cardProto.getCardTypeCase())
		{
			case SUIT_CARD:
				ProtoUtils.SuitCard suitCardProto = cardProto.getSuitCard();
				return new SuitCard(suitCardProto.getSuit(), suitCardProto.getValue());
			case TAROCK_CARD:
				ProtoUtils.TarockCard tarockCardProto = cardProto.getTarockCard();
				return new TarockCard(tarockCardProto.getValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	private AnnouncementContra createAnnouncementFromProto(ProtoUtils.Announcement announcementProto)
	{
		return new AnnouncementContra(Announcements.getFromID(announcementProto.getId()), announcementProto.getContraLevel());
	}
}
