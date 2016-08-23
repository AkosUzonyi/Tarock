package com.tisza.tarock.card;

public class TarockCard extends Card
{
	private int value;
	
	public TarockCard(int v)
	{
		if (v < 1 || v >= 23) throw new IllegalArgumentException();
		value = v;
	}
	
	public int getValue()
	{
		return value;
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
	
	public int hashCode()
	{
		return value;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof TarockCard)) return false;
		TarockCard other = (TarockCard)o;
		return value == other.value;
	}

	public int getPoints()
	{
		return isHonor() ? 5 : 1;
	}
	
	public String toString()
	{
		return "Tarock " + getValue();
	}
}
