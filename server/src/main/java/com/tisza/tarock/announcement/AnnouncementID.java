package com.tisza.tarock.announcement;

import com.tisza.tarock.card.*;

import java.util.*;

public class AnnouncementID
{
	private String name;
	private int suit = -1;
	private Card card = null;
	private int round = -1;

	public AnnouncementID(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public boolean hasSuit()
	{
		return suit >= 0;
	}

	public int getSuit()
	{
		return suit;
	}

	public AnnouncementID setSuit(int suit)
	{
		this.suit = suit;
		return this;
	}

	public boolean hasCard()
	{
		return card != null;
	}

	public Card getCard()
	{
		return card;
	}

	public AnnouncementID setCard(Card card)
	{
		this.card = card;
		return this;
	}

	public boolean hasRound()
	{
		return round >= 0;
	}

	public int getRound()
	{
		return round;
	}

	public AnnouncementID setRound(int round)
	{
		this.round = round;
		return this;
	}

	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AnnouncementID that = (AnnouncementID)o;
		return suit == that.suit && round == that.round && Objects.equals(name, that.name) && Objects.equals(card, that.card);
	}

	public int hashCode()
	{
		return Objects.hash(name, suit, card, round);
	}
}
