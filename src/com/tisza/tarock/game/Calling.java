package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Calling
{
	private final int callerPlayer;
	private PlayerPairs playerPairs = null;
	private Invitation invit;
	
	private AllPlayersCards cards;
	
	public Calling(AllPlayersCards cards, int callerPlayer, Bidding.Invitation invit)
	{
		this.callerPlayer = callerPlayer;
		this.cards = cards;
		this.invit = invit;
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
			callOptions.add(new TarockCard(19));
		}
		
		if (invit == Invitation.XVIII)
		{
			callOptions.add(new TarockCard(18));
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
