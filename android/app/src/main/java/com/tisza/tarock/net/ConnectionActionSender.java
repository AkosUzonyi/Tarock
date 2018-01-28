package com.tisza.tarock.net;

import com.tisza.tarock.Announcement;
import com.tisza.tarock.card.Card;
import com.tisza.tarock.message.ActionSender;
import com.tisza.tarock.message.Utils;
import com.tisza.tarock.net.packet.PacketAction;
import com.tisza.tarock.proto.ActionProto.Action;

import java.util.List;

public class ConnectionActionSender implements ActionSender
{
	private Connection connection;

	public ConnectionActionSender(Connection connection)
	{
		this.connection = connection;
	}

	private void doAction(Action action)
	{
		connection.sendPacket(new PacketAction(action));
	}

	public void announce(Announcement announcement)
	{
		doAction(Action.newBuilder().setAnnounce(Action.Announce.newBuilder().setAnnouncement(Utils.announcementToProto(announcement))).build());
	}

	public void announcePassz()
	{
		doAction(Action.newBuilder().setAnnoucePassz(Action.AnnouncePassz.newBuilder()).build());
	}

	public void bid(int bid)
	{
		doAction(Action.newBuilder().setBid(Action.Bid.newBuilder().setBid(bid)).build());
	}

	public void call(Card card)
	{
		doAction(Action.newBuilder().setCall(Action.Call.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	public void change(List<Card> cards)
	{
		doAction(Action.newBuilder().setChange(Action.Change.newBuilder().addAllCard(Utils.cardListToProto(cards))).build());
	}

	public void playCard(Card card)
	{
		doAction(Action.newBuilder().setPlayCard(Action.PlayCard.newBuilder().setCard(Utils.cardToProto(card))).build());
	}

	public void readyForNewGame()
	{
		doAction(Action.newBuilder().setReadyForNewGame(Action.ReadyForNewGame.newBuilder()).build());
	}

	public void throwCards()
	{
		doAction(Action.newBuilder().setThrowCards(Action.ThrowCards.newBuilder()).build());
	}
}
