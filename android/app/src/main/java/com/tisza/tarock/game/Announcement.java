package com.tisza.tarock.game;

import android.content.res.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.gui.misc.*;

import java.util.*;

public class Announcement
{
	private final String id;
	private final String name;
	private final int contraLevel;
	private final int suit;
	private final Card card;
	private final int trick;

	private Announcement(String id, String name, int suit, Card card, int trick, int contraLevel)
	{
		this.id = id;
		this.name = name;
		this.suit = suit;
		this.card = card;
		this.trick = trick;
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
		int contraLevel = 0;
		int suit = -1;
		Card card = null;
		int trick = -1;

		int pos = 0;

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
				case 'T':
					try
					{
						trick = Integer.parseInt(substr);
					}
					catch (NumberFormatException e)
					{
						throw new IllegalArgumentException("invalid trick number: " + trick);
					}
					break;
				case 'K':
					contraLevel = substr.equals("s") ? -1 : Integer.parseInt(substr);
					break;
				default:
					throw new IllegalArgumentException("invalid announcement modifier: " + c);
			}
		}

		return new Announcement(id, name, suit, card, trick, contraLevel);
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

	public String translate(Resources resources)
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
		if (hasTrick())
			builder.appendWord(ResourceMappings.trickNames[getTrick()]);

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

	public boolean hasCard()
	{
		return card != null;
	}

	public Card getCard()
	{
		return card;
	}

	public boolean hasTrick()
	{
		return trick >= 0;
	}

	public int getTrick()
	{
		return trick;
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
		if (trick != that.trick)
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
		result = 31 * result + trick;
		result = 31 * result + contraLevel;
		return result;
	}
}
