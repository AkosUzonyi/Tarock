package com.tisza.tarock.card.sort;

import java.util.*;

import com.tisza.tarock.card.*;

public class IDSort implements Comparator<Card>
{
	public int compare(Card c0, Card c1)
	{
		return c0.getID() - c1.getID();
	}
}
