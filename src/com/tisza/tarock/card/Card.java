package com.tisza.tarock.card;

import java.util.*;

public abstract class Card
{
	public static final Collection<Card> all;
	public static final Collection<Card> honors;
	
	public abstract int getPoints();
	public abstract int getID();
	public abstract boolean doesBeat(Card otherCard);
	
	public boolean isHonor()
	{
		return honors.contains(this);
	}
	
	public static Card fromId(int id)
	{
		if (!isValidId(id))
			throw new IllegalArgumentException();
		
		if (id < 20)
		{
			int suit = id % 4;
			int value = id / 4 + 1;
			return new SuitCard(suit, value);
		}
		else
		{
			int value = id - 20 + 1;
			return new TarockCard(value);
		}
	}
	
	public static boolean isValidId(int id)
	{
		return id >= 0 && id < 42;
	}
	
	public int hashCode()
	{
		return getID();
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof SuitCard)) return false;
		Card other = (Card)o;
		return getID() == other.getID();
	}
	
	static
	{
		Collection<Card> honorsLocal = new ArrayList<Card>();
		honorsLocal.add(new TarockCard(1));
		honorsLocal.add(new TarockCard(21));
		honorsLocal.add(new TarockCard(22));
		honors = Collections.unmodifiableCollection(honorsLocal);
		
		Collection<Card> allLocal = new ArrayList<Card>();
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				allLocal.add(new SuitCard(s, v));
			}
		}
		for (int v = 1; v <= 22; v++)
		{
			allLocal.add(new TarockCard(v));
		}
		all = Collections.unmodifiableCollection(allLocal);
	}
}
