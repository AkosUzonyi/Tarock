package com.tisza.tarock.game;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.gui.*;
import com.tisza.tarock.message.*;

import java.util.*;

public class Announcement implements Comparable<Announcement>, ActionButtonItem
{
	private final String id;
	private final String name;
	private final int contraLevel;
	private final int suit;
	private final Card card;
	private final int round;

	private Announcement(String id, String name, int suit, Card card, int round, int contraLevel)
	{
		this.id = id;
		this.name = name;
		this.suit = suit;
		this.card = card;
		this.round = round;
		this.contraLevel = contraLevel;
	}

	public String getID()
	{
		return id;
	}

	public static Announcement fromID(String id)
	{
		if (id == null)
			throw new NullPointerException();

		String name;
		int contraLevel;
		int suit = -1;
		Card card = null;
		int round = -1;

		int pos = 0;

		char contraLevelChar = id.charAt(pos++);
		if (contraLevelChar == 's')
			contraLevel = -1;
		else
			if (Character.isDigit(contraLevelChar))
				contraLevel = Character.digit(contraLevelChar, 10);
			else
				throw new IllegalArgumentException("invalid card id: " + id);

		name = id.substring(pos, pos = nextUppercase(id, pos));

		while (pos < id.length())
		{
			char c = id.charAt(pos++);
			String substr = id.substring(pos, pos = nextUppercase(id, pos));

			switch (c)
			{
				case 'S':
					suit = parseSuit(substr);
					break;
				case 'C':
					card = Card.fromId(substr);
					break;
				case 'R':
					try
					{
						round = Integer.parseInt(substr);
					}
					catch (NumberFormatException e)
					{
						throw new IllegalArgumentException("invalid round number: " + round);
					}
					break;
				default:
					throw new IllegalArgumentException("invalid announcement modifier: " + c);
			}
		}

		return new Announcement(id, name, suit, card, round, contraLevel);
	}

	private static int parseSuit(String str)
	{
		if (str.length() != 1)
			throw new IllegalArgumentException("invalid suit: " + str);

		char c = str.charAt(0);

		if (c < 'a' || c > 'd')
			throw new IllegalArgumentException("invalid suit: " + str);

		return c - 'a';
	}

	private static int nextUppercase(String str, int from)
	{
		int i;
		for (i = from; i < str.length(); i++)
			if (Character.isUpperCase(str.charAt(i)))
				break;
		return i;
	}

	public String toString()
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

	@Override
	public Action getAction()
	{
		return Action.announce(this);
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

	public boolean hasCard()
	{
		return card != null;
	}

	public Card getCard()
	{
		return card;
	}

	public boolean hasRound()
	{
		return round >= 0;
	}

	public int getRound()
	{
		return round;
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
			return card.compareTo(other.card);

		if (hasRound() && other.hasRound() && round != other.round)
			return (round - other.round) * (name.equals("ultimo") ? -1 : 1);

		return 0;
	}
}
