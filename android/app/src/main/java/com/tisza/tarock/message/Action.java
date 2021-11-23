package com.tisza.tarock.message;

import android.content.res.*;
import android.text.*;
import android.util.*;
import com.tisza.tarock.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.gui.*;

import java.util.*;

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
		return new Action("fold:" + TextUtils.join(",", Utils.map(cards, Card::getID)));
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
			case "fold":
				List<Card> cards = new ArrayList<>();
				for (String cardID : actionParams.split(","))
					if (!cardID.isEmpty())
						cards.add(Card.fromId(cardID));
				handler.fold(player, cards);
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
			default:
				Log.w("Action", "invalid action: " + actionType);
		}
	}

	public String translate(Resources resources)
	{
		int colonIndex = id.indexOf(":");
		String actionType = id.substring(0, colonIndex);
		String actionParams = id.substring(colonIndex + 1);
		switch (actionType)
		{
			case "bid":
				int bid = actionParams.equals("p") ? -1 : Integer.parseInt(actionParams);
				return ResourceMappings.bidToName.get(bid);
			case "fold":
				StringBuilder msg = null;
				for (String cardID : actionParams.split(","))
				{
					if (cardID.isEmpty())
						continue;

					Card card = Card.fromId(cardID);
					String cardName = ResourceMappings.uppercaseCardName(card);
					if (msg == null)
						msg = new StringBuilder(cardName);
					else
						msg.append(", ").append(cardName);
				}

				if (msg == null)
					return null;

				return resources.getString(R.string.message_fold, msg.toString());
			case "call":
				return ResourceMappings.uppercaseCardName(Card.fromId(actionParams));
			case "announce":
				if (actionParams.equals("passz"))
					return resources.getString(R.string.passz);
				else
					return Announcement.fromID(actionParams).translate(resources);
			case "throw":
				return resources.getString(R.string.message_cards_thrown);
		}
		return null;
	}

	public PhaseEnum getPhase()
	{
		int colonIndex = id.indexOf(":");
		String actionType = id.substring(0, colonIndex);
		switch (actionType)
		{
			case "bid": return PhaseEnum.BIDDING;
			case "fold": return PhaseEnum.FOLDING;
			case "call": return PhaseEnum.CALLING;
			case "announce": return PhaseEnum.ANNOUNCING;
			case "play": return PhaseEnum.GAMEPLAY;
			case "newgame": return PhaseEnum.END;
			case "throw": return PhaseEnum.INTERRUPTED;
			default:
				Log.w("Action", "invalid action: " + actionType);
				return null;
		}
	}

	public String getParams()
	{
		return id.substring(id.indexOf(":") + 1);
	}
}
