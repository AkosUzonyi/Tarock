package com.tisza.tarock;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.gui.ResourceMappings;

public class Announcement
{
	private String name;
	private int suit = -1;
	private Card card = null;
	private int round = -1;
	private int contraLevel;

	public Announcement(String name, int contraLevel)
	{
		this.name = name;
		this.contraLevel = contraLevel;
	}

	public String getDisplayText()
	{
		SentenceBuilder builder = new SentenceBuilder();

		if (isSilent())
			builder.appendWord(ResourceMappings.silent);
		else
			builder.appendWord(ResourceMappings.contraNames[getContraLevel()]);

		if (hasSuit())
			builder.appendWord(ResourceMappings.suitNames[getSuit()]);
		if (hasCard())
			builder.appendWord(ResourceMappings.cardToName.get(getCard()));
		if (hasRound())
			builder.appendWord(ResourceMappings.roundNames[getRound()]);

		String nameText = ResourceMappings.getAnnouncementNameText(getName());
		if (nameText == null)
			nameText = "[" + getName() + "]";
		builder.appendWord(nameText);

		return builder.toString();
	}

	public String getName()
	{
		return name;
	}

	public boolean isSilent()
	{
		return contraLevel < 0;
	}

	public int getContraLevel()
	{
		return contraLevel;
	}

	public boolean hasSuit()
	{
		return suit >= 0;
	}

	public int getSuit()
	{
		return suit;
	}

	public void setSuit(int suit)
	{
		this.suit = suit;
	}

	public boolean hasCard()
	{
		return card != null;
	}

	public Card getCard()
	{
		return card;
	}

	public void setCard(Card card)
	{
		this.card = card;
	}

	public boolean hasRound()
	{
		return round >= 0;
	}

	public int getRound()
	{
		return round;
	}

	public void setRound(int round)
	{
		this.round = round;
	}
}
