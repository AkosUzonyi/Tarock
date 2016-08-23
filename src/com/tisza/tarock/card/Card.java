package com.tisza.tarock.card;

import java.util.*;

public abstract class Card
{
	public static final List<Card> honors = new ArrayList<Card>();
	public static final List<Card> all = new ArrayList<Card>();
	
	public abstract int getPoints();
	public abstract boolean doesBeat(Card otherCard);
	
	public boolean isHonor()
	{
		return honors.contains(this);
	}
	
	static
	{
		honors.add(new TarockCard(1));
		honors.add(new TarockCard(21));
		honors.add(new TarockCard(22));
		
		for (int s = 0; s < 4; s++)
		{
			for (int v = 1; v <= 5; v++)
			{
				all.add(new SuitCard(s, v));
			}
		}
		for (int v = 1; v <= 22; v++)
		{
			all.add(new TarockCard(v));
		}
	}
}
