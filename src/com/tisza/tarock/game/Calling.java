package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Calling
{
	private final int callerPlayer;
	private int calledPlayer = -1;
	private boolean isSoloIntentional = false;
	
	private AllPlayersCards cards;
	
	private Collection<Card> callOptions = new ArrayList<Card>();
	
	public Calling(AllPlayersCards cards, int callerPlayer, Bidding.Invitation invit)
	{
		this.callerPlayer = callerPlayer;
		this.cards = cards;
		
		PlayerCards pc = cards.getPlayerCards(callerPlayer);
		for (int t = 20; t >= 1; t++)
		{
			TarockCard c = new TarockCard(t);
			if (!pc.hasCard(c))
			{
				callOptions.add(c);
				break;
			}
		}
		
		callOptions.addAll(pc.filter(new TarockFilter()));
	}
	
	public boolean call(Card card, int player)
	{
		if (player != callerPlayer)
			return false;
		
		if (!callOptions.contains(card))
			return false;
		
		for (int i = 0; i < 4; i++)
		{
			PlayerCards pc = cards.getPlayerCards(i);
			if (pc.hasCard(card))
			{
				calledPlayer = i;
			}
		}
		
		if (calledPlayer < 0)
		{
			calledPlayer = callerPlayer;
			isSoloIntentional = true;
		}
		
		return true;
	}
	
	public Collection<Card> getCallableTarocks()
	{
		return Collections.unmodifiableCollection(callOptions);
	}
	
	public int getCalledPlayer()
	{
		if (calledPlayer < 0)
			throw new IllegalStateException();
		return calledPlayer;
	}
	
	public boolean isSolo()
	{
		if (calledPlayer < 0)
			throw new IllegalStateException();
		return calledPlayer == callerPlayer;
	}
	
	public boolean isSoloIntentional()
	{
		if (calledPlayer < 0)
			throw new IllegalStateException();
		return isSoloIntentional;
	}
}
