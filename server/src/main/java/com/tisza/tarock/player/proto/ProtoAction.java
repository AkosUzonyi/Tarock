package com.tisza.tarock.player.proto;

import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.*;

import java.util.stream.*;

class ProtoAction implements Action
{
	private final ActionProto.Action actionProto;
	private final int player;

	public ProtoAction(int player, ActionProto.Action actionProto)
	{
		this.player = player;
		this.actionProto = actionProto;
	}

	@Override
	public void handle(ActionHandler handler)
	{
		switch (actionProto.getActionTypeCase())
		{
			case BID:
				handler.bid(player, actionProto.getBid().getBid());
				break;
			case CHANGE:
				handler.change(player, actionProto.getChange().getCardList().stream().map(Utils::cardFromProto).collect(Collectors.toList()));
				break;
			case CALL:
				handler.call(player, Utils.cardFromProto(actionProto.getCall().getCard()));
				break;
			case ANNOUNCE:
				handler.announce(player, Utils.announcementFromProto(actionProto.getAnnounce().getAnnouncement()));
				break;
			case ANNOUCE_PASSZ:
				handler.announcePassz(player);
				break;
			case PLAY_CARD:
				handler.playCard(player, Utils.cardFromProto(actionProto.getPlayCard().getCard()));
				break;
			case READY_FOR_NEW_GAME:
				handler.readyForNewGame(player);
				break;
			case THROW_CARDS:
				handler.throwCards(player);
			default:
				System.err.println("unkown action: " + actionProto.getActionTypeCase());
				break;
		}

	}
}
