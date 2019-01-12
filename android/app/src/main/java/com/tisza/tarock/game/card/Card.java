package com.tisza.tarock.game.card;

import com.tisza.tarock.game.*;
import com.tisza.tarock.gui.*;
import com.tisza.tarock.message.*;

import java.util.*;

public abstract class Card implements ActionButtonItem
{
	private static final Collection<Card> all = new ArrayList<>();
	private static final TarockCard[] tarockCards = new TarockCard[22];
	private static final SuitCard[][] suitCards = new SuitCard[4][5];

	public abstract int getPoints();
	public abstract int getID();
	public abstract boolean isHonor();
	public abstract boolean doesBeat(Card otherCard);

	public int hashCode()
	{
		return getID();
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof Card)) return false;
		Card other = (Card)o;
		return getID() == other.getID();
	}

	public static Collection<Card> getAll()
	{
		return all;
	}

	public static TarockCard getTarockCard(int value)
	{
		return tarockCards[value - 1];
	}

	public static SuitCard getSuitCard(int suit, int value)
	{
		return suitCards[suit][value - 1];
	}

	public static Card fromId(int id)
	{
		if (!isValidId(id))
			throw new IllegalArgumentException();
		
		if (id < 20)
		{
			int suit = id / 5;
			int value = id % 5 + 1;
			return getSuitCard(suit, value);
		}
		else
		{
			int value = id - 20 + 1;
			return getTarockCard(value);
		}
	}
	
	private static boolean isValidId(int id)
	{
		return id >= 0 && id < 42;
	}

	@Override
	public void doAction(ActionSender actionSender)
	{
		actionSender.call(this);
	}

	@Override
	public String toString()
	{
		return ResourceMappings.cardToName.get(this);
	}

	static
	{
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				SuitCard suitCard = new SuitCard(s, v);
				suitCards[s][v - 1] = suitCard;
				all.add(suitCard);
			}
		}
		for (int v = 1; v <= 22; v++)
		{
			TarockCard tarockCard = new TarockCard(v);
			tarockCards[v - 1] = tarockCard;
			all.add(tarockCard);
		}
	}
}
