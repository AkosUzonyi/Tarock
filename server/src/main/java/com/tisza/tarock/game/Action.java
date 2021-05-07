package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;

import java.util.*;
import java.util.stream.*;

public class Action
{
	private final String id;

	public Action(String id)
	{
		this.id = id;

		if (id.length() >= 1024)
			throw new IllegalArgumentException("action id length >= 1024: " + id.length());
	}

	public String getId()
	{
		return id;
	}

	public static Action bid(int bid)
	{
		return new Action("bid:" + (bid < 0 ? "p" : bid));
	}

	public static Action fold(List<Card> cards)
	{
		return new Action("fold:" + cards.stream().map(Card::getID).collect(Collectors.joining(",")));
	}

	public static Action call(Card card)
	{
		return new Action("call:" + card.getID());
	}

	public static Action announce(AnnouncementContra announcement)
	{
		return new Action("announce:" + announcement.getID());
	}

	public static Action announcePassz()
	{
		return new Action("announce:passz");
	}

	public static Action play(Card card)
	{
		return new Action("play:" + card.getID());
	}

	public static Action readyForNewGame()
	{
		return new Action("newgame:");
	}

	public static Action throwCards()
	{
		return new Action("throw:");
	}

	public boolean handle(PlayerSeat player, ActionHandler handler)
	{
		int colonIndex = id.indexOf(":");
		String actionType = id.substring(0, colonIndex);
		String actionParams = id.substring(colonIndex + 1);
		switch (actionType)
		{
			case "bid":
				int bid = actionParams.equals("p") ? -1 : Integer.parseInt(actionParams);
				return handler.bid(player, bid);
			case "fold":
				return handler.fold(player, Arrays.stream(actionParams.split(",")).filter(s -> !s.isEmpty()).map(Card::fromId).collect(Collectors.toList()));
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
			default:
				throw new IllegalArgumentException("invalid action: " + actionType);
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Action))
			return false;

		return id.equals(((Action)o).id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
