package com.tisza.tarock.message;

import android.text.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.net.*;

import java.util.*;

public class Action
{
	private final String id;

	public Action(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public static Action bid(int bid)
	{
		return new Action("bid:" + (bid < 0 ? "p" : bid));
	}

	public static Action skart(List<Card> cards)
	{
		return new Action("skart:" + TextUtils.join(",", Utils.map(cards, Card::getID)));
	}

	public static Action call(Card card)
	{
		return new Action("call:" + card.getID());
	}

	public static Action announce(Announcement announcement)
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

	public static Action chat(String msg)
	{
		return new Action("chat:" + msg);
	}

	public void handle(EventHandler handler, int player)
	{
		int colonIndex = id.indexOf(":");
		String actionType = id.substring(0, colonIndex);
		String actionParams = id.substring(colonIndex + 1);
		switch (actionType)
		{
			case "bid":
				int bid = actionParams.equals("p") ? -1 : Integer.parseInt(actionParams);
				handler.bid(player, bid);
				break;
			case "skart":
				System.err.println("warning: skart action arrived");
				break;
			case "call":
				handler.call(player, Card.fromId(actionParams));
				break;
			case "announce":
				if (actionParams.equals("passz"))
					handler.announcePassz(player);
				else
					handler.announce(player, Announcement.fromID(actionParams));
				break;
			case "play":
				handler.playCard(player, Card.fromId(actionParams));
				break;
			case "newgame":
				handler.readyForNewGame(player);
				break;
			case "throw":
				handler.throwCards(player);
				break;
			case "chat":
				handler.chat(player, actionParams);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}
}
