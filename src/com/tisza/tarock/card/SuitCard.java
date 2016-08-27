package com.tisza.tarock.card;

public class SuitCard extends Card
{
	private int suit, value;

	public SuitCard(int suit, int value)
	{
		if (suit < 0 || suit >= 4 || value < 1 || value >= 6)
			throw new IllegalArgumentException();
		this.suit = suit;
		this.value = value;
	}

	public int getSuit()
	{
		return suit;
	}

	public int getValue()
	{
		return value;
	}

	public int getPoints()
	{
		return value;
	}
	
	public boolean doesBeat(Card otherCard)
	{
		if (otherCard instanceof SuitCard)
		{
			SuitCard otherSuitCard = (SuitCard)otherCard;
			return suit == otherSuitCard.suit && value > otherSuitCard.value;
		}
		else if (otherCard instanceof TarockCard)
		{
			return false;
		}
		else
		{
			throw new IllegalArgumentException("Unknown card type: " + otherCard.getClass().getName());
		}
	}

	public int getID()
	{
		return suit * 5 + value - 1;
	}
	
	public int hashCode()
	{
		return suit * 5 + value;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof SuitCard)) return false;
		SuitCard other = (SuitCard)o;
		return suit == other.suit && value == other.value;
	}
	
	public String toString()
	{
		return "Suit " + getSuit() + "-" + getValue();
	}
}
