package com.tisza.tarock.game.card;

public class SuitCard extends Card
{
	private final int suit, value;

	SuitCard(int suit, int value)
	{
		checkSuitValid(suit);
		if (value < 1 || value >= 6)
			throw new IllegalArgumentException("invalid suit card value: " + value);
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

	@Override
	public int getPoints()
	{
		return value;
	}

	@Override
	public boolean isHonor()
	{
		return false;
	}

	@Override
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
			throw new IllegalArgumentException("invalid card type: " + otherCard.getClass().getName());
		}
	}

	@Override
	public int compareTo(Card otherCard)
	{
		if (otherCard instanceof TarockCard)
			return -1;

		SuitCard otherSuitCard = (SuitCard)otherCard;

		if (suit != otherSuitCard.suit)
			return suit - otherSuitCard.suit;

		if (value != otherSuitCard.value)
			return value - otherSuitCard.value;

		return 0;
	}

	@Override
	public String getID()
	{
		return suitToString(suit) + value;
	}

	public String toString()
	{
		return "Suit " + getSuit() + "-" + getValue();
	}

	public static void checkSuitValid(int suit)
	{
		if (suit < 0 || suit >= 4)
			throw new IllegalArgumentException("invalid suit: " + suit);
	}

	public static String suitToString(int suit)
	{
		checkSuitValid(suit);
		return String.valueOf((char)('a' + suit));
	}
}
