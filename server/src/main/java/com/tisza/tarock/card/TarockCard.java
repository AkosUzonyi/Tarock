package com.tisza.tarock.card;

public class TarockCard extends Card
{
	private int value;
	
	TarockCard(int v)
	{
		if (v < 1 || v >= 23) throw new IllegalArgumentException();
		value = v;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public int getPoints()
	{
		return isHonor() ? 5 : 1;
	}

	public boolean isHonor()
	{
		return value == 1 || value == 21 || value == 22;
	}

	public boolean doesBeat(Card otherCard)
	{
		if (otherCard instanceof SuitCard)
		{
			return true;
		}
		else if (otherCard instanceof TarockCard)
		{
			TarockCard otherTarockCard = (TarockCard)otherCard;
			return value > otherTarockCard.value;
		}
		else
		{
			throw new IllegalArgumentException("Unknown card type: " + otherCard.getClass().getName());
		}
	}

	public int getID()
	{
		return value - 1 + 20;
	}

	public String toString()
	{
		return "Tarock " + getValue();
	}
}
