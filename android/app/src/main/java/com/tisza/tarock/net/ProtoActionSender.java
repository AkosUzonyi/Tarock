package com.tisza.tarock.net;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.message.*;
import com.tisza.tarock.proto.ActionProto.*;
import com.tisza.tarock.proto.*;

import java.util.*;

public class ProtoActionSender implements ActionSender
{
	private ProtoConnection connection;

	public ProtoActionSender(ProtoConnection connection)
	{
		this.connection = connection;
	}

	private void doAction(Action action)
	{
		connection.sendMessage(MainProto.Message.newBuilder().setAction(action).build());
	}

	@Override
	public void announce(Announcement announcement)
	{
		doAction(Action.newBuilder().setAnnounce(Action.Announce.newBuilder().setAnnouncement(announcement.getID())).build());
	}

	@Override
	public void announcePassz()
	{
		doAction(Action.newBuilder().setAnnoucePassz(Action.AnnouncePassz.newBuilder()).build());
	}

	@Override
	public void bid(int bid)
	{
		doAction(Action.newBuilder().setBid(Action.Bid.newBuilder().setBid(bid)).build());
	}

	@Override
	public void call(Card card)
	{
		doAction(Action.newBuilder().setCall(Action.Call.newBuilder().setCard(card.getID())).build());
	}

	@Override
	public void change(List<Card> cards)
	{
		Action.Change.Builder changeAction = Action.Change.newBuilder();
		for (Card card : cards)
			changeAction.addCard(card.getID());
		doAction(Action.newBuilder().setChange(changeAction).build());
	}

	@Override
	public void playCard(Card card)
	{
		doAction(Action.newBuilder().setPlayCard(Action.PlayCard.newBuilder().setCard(card.getID())).build());
	}

	@Override
	public void readyForNewGame()
	{
		doAction(Action.newBuilder().setReadyForNewGame(Action.ReadyForNewGame.newBuilder()).build());
	}

	@Override
	public void throwCards()
	{
		doAction(Action.newBuilder().setThrowCards(Action.ThrowCards.newBuilder()).build());
	}

	@Override
	public void chat(String message)
	{
		doAction(Action.newBuilder().setChat(Action.Chat.newBuilder().setMessage(message)).build());
	}
}
