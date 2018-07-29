package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.gui.*;

import java.util.*;

public class Announcement implements Comparable<Announcement>
{
	private String name;
	private int suit = -1;
	private Card card = null;
	private int round = -1;
	private int contraLevel;

	public Announcement(String name, int contraLevel)
	{
		if (name == null)
			throw new NullPointerException();

		this.name = name;
		this.contraLevel = contraLevel;
	}

	public String getDisplayText()
	{
		SentenceBuilder builder = new SentenceBuilder();

		if (isSilent())
			builder.appendWord(ResourceMappings.silent);
		else
			builder.appendWord(ResourceMappings.contraNames[getContraLevel()]);

		if (hasSuit())
			builder.appendWord(ResourceMappings.suitNames[getSuit()]);
		if (hasCard())
			builder.appendWord(ResourceMappings.cardToName.get(getCard()));
		if (hasRound())
			builder.appendWord(ResourceMappings.roundNames[getRound()]);

		String nameText = ResourceMappings.getAnnouncementNameText(getName());
		if (nameText == null)
			nameText = "[" + getName() + "]";
		builder.appendWord(nameText);

		return builder.toString();
	}

	public String getName()
	{
		return name;
	}

	public boolean isSilent()
	{
		return contraLevel < 0;
	}

	public int getContraLevel()
	{
		return contraLevel;
	}

	public boolean hasSuit()
	{
		return suit >= 0;
	}

	public int getSuit()
	{
		return suit;
	}

	public void setSuit(int suit)
	{
		this.suit = suit;
	}

	public boolean hasCard()
	{
		return card != null;
	}

	public Card getCard()
	{
		return card;
	}

	public void setCard(Card card)
	{
		this.card = card;
	}

	public boolean hasRound()
	{
		return round >= 0;
	}

	public int getRound()
	{
		return round;
	}

	public void setRound(int round)
	{
		this.round = round;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Announcement that = (Announcement)o;

		if (suit != that.suit)
			return false;
		if (round != that.round)
			return false;
		if (contraLevel != that.contraLevel)
			return false;
		if (name != null ? !name.equals(that.name) : that.name != null)
			return false;
		return card != null ? card.equals(that.card) : that.card == null;
	}

	@Override
	public int hashCode()
	{
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + suit;
		result = 31 * result + (card != null ? card.hashCode() : 0);
		result = 31 * result + round;
		result = 31 * result + contraLevel;
		return result;
	}

	private static final List<String> order = Arrays.asList(
		"jatek",
		"hkp",
		"nyolctarokk",
		"kilenctarokk",
		"trull",
		"negykiraly",
		"banda",
		"dupla",
		"hosszudupla",
		"kezbevacak",
		"szinesites",
		"volat",
		"kiralyultimo",
		"ketkiralyok",
		"haromkiralyok",
		"zaroparos",
		"facan",
		"xxifogas",
		"ultimo",
		"kisszincsalad",
		"nagyszincsalad"
	);

	@Override
	public int compareTo(Announcement other)
	{
		if (contraLevel != other.contraLevel)
			return other.contraLevel - contraLevel;

		if (!name.equals(other.name))
		{
			int myIndex = order.indexOf(name);
			int otherIndex = order.indexOf(other.name);

			if (myIndex < 0 || otherIndex < 0)
				return name.compareTo(other.name);

			return myIndex - otherIndex;
		}

		if (hasSuit() && other.hasSuit() && suit != other.suit)
			return suit - other.suit;

		if (hasCard() && other.hasCard() && !card.equals(other.card))
			return card.getID() - other.card.getID();

		if (hasRound() && other.hasRound() && round != other.round)
			return (round - other.round) * (name.equals("ultimo") ? -1 : 1);

		return 0;
	}
}
