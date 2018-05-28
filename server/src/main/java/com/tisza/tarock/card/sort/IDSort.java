package com.tisza.tarock.card.sort;

import com.tisza.tarock.card.*;

import java.util.*;

public class IDSort implements Comparator<Card>
{
	@Override
	public int compare(Card c0, Card c1)
	{
		return c0.getID() - c1.getID();
	}
}

