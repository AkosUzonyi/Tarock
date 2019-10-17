package com.tisza.tarock.message;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;

import java.util.*;
import java.util.stream.*;

public class Action
{
	private final String id;
	private final PlayerSeat player;

	public Action(PlayerSeat player, String id)
	{
		this.id = id;
		this.player = player;

		if (id.length() >= 256)
			throw new IllegalArgumentException("action id length >= 1024: " + id.length());
	}

	public String getId()
	{
		return id;
	}

	public static Action bid(PlayerSeat player, int bid)
	{
		return new Action(player, "bid:" + (bid < 0 ? "p" : bid));
	}

	public static Action skart(PlayerSeat player, List<Card> cards)
	{
		return new Action(player,  "skart:" + cards.stream().map(Card::getID).collect(Collectors.joining(",")));
	}

	public static Action call(PlayerSeat player, Card card)
	{
		return new Action(player,  "call:" + card.getID());
	}

	public static Action announce(PlayerSeat player, AnnouncementContra announcement)
	{
		return new Action(player,  "announce:" + announcement.getID());
	}

	public static Action announcePassz(PlayerSeat player)
	{
		return new Action(player,  "announce:passz");
	}

	public static Action play(PlayerSeat player, Card card)
	{
		return new Action(player,  "play:" + card.getID());
	}

	public static Action readyForNewGame(PlayerSeat player)
	{
		return new Action(player,  "newgame:");
	}

	public static Action throwCards(PlayerSeat player)
	{
		return new Action(player,  "throw:");
	}

	public static Action chat(PlayerSeat player, String msg)
	{
		return new Action(player,  "chat:" + msg);
	}

	public boolean handle(ActionHandler handler)
	{
		int colonIndex = id.indexOf(":");
		String actionType = id.substring(0, colonIndex);
		String actionParams = id.substring(colonIndex + 1);
		switch (actionType)
		{
			case "bid":
				int bid = actionParams.equals("p") ? -1 : Integer.parseInt(actionParams);
				return handler.bid(player, bid);
			case "skart":
				return handler.change(player, Arrays.stream(actionParams.split(",")).filter(s -> !s.isEmpty()).map(Card::fromId).collect(Collectors.toList()));
			case "call":
				return handler.call(player, Card.fromId(actionParams));
			case "announce":
				if (actionParams.equals("passz"))
					return handler.announcePassz(player);
				else
					return handler.announce(player, AnnouncementContra.fromID(actionParams));
			case "play":
				return handler.playCard(player, Card.fromId(actionParams));
			case "newgame":
				return handler.readyForNewGame(player);
			case "throw":
				return handler.throwCards(player);
			case "chat":
				return handler.chat(player, actionParams);
			default:
				throw new IllegalArgumentException("invalid action: " + actionType);
		}
	}
}
