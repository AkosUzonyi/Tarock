package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Calling
{
	private final int callerPlayer;
	
	private AllPlayersCards cards;
	private PlayerPairs playerPairs = null;
	private Invitation invit;
	private boolean canCallAnyTarock;
	
	public Calling(AllPlayersCards cards, int callerPlayer, Bidding.Invitation invit, int[] tarockSkartedCounts)
	{
		this.callerPlayer = callerPlayer;
		this.cards = cards;
		this.invit = invit;
		canCallAnyTarock = false;
		for (int i = 0; i < 4; i++)
		{
			if (i != callerPlayer && tarockSkartedCounts[i] > 0)
			{
				canCallAnyTarock = true;
			}
		}
	}
	
	public int getCaller()
	{
		return callerPlayer;
	}
	
	public boolean call(int player, Card card)
	{
		if (isFinished())
			return false;
		
		if (player != callerPlayer)
			return false;
		
		if (!getCallableCards().contains(card))
			return false;
		
		int calledPlayer = -1;
		for (int i = 0; i < 4; i++)
		{
			PlayerCards pc = cards.getPlayerCards(i);
			if (pc.hasCard(card))
			{
				calledPlayer = i;
			}
		}
		
		boolean isSoloIntentional = false;
		if (calledPlayer < 0)
		{
			calledPlayer = callerPlayer;
			isSoloIntentional = true;
		}
		
		playerPairs = new PlayerPairs(callerPlayer, calledPlayer, isSoloIntentional);
		
		return true;
	}
	
	public List<Card> getCallableCards()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		List<Card> callOptions = new ArrayList<Card>();
		
		if (canCallAnyTarock)
		{
			CardFilter cf = new CallableCardFilter();
			for (Card c : Card.all)
			{
				if (cf.match(c))
				{
					callOptions.add(c);
				}
			}
			return callOptions;
		}
		
		PlayerCards pc = cards.getPlayerCards(callerPlayer);
		for (int t = 20; t >= 1; t--)
		{
			TarockCard c = new TarockCard(t);
			if (!pc.hasCard(c))
			{
				callOptions.add(c);
				break;
			}
		}
		
		if (invit == Invitation.XIX)
		{
			Card c = new TarockCard(19);
			if (!callOptions.contains(c))
			{
				callOptions.add(c);
			}
		}
		
		if (invit == Invitation.XVIII)
		{
			Card c = new TarockCard(18);
			if (!callOptions.contains(c))
			{
				callOptions.add(c);
			}
		}
		
		callOptions.addAll(pc.filter(new CallableCardFilter()));
		return callOptions;
	}
	
	public boolean isFinished()
	{
		return playerPairs != null;
	}
	
	public PlayerPairs getPlayerPairs()
	{
		if (!isFinished())
			throw new IllegalStateException();
		return playerPairs;
	}
}
