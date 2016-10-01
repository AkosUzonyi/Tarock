package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.Invitation;

public class Calling
{
	private final int callerPlayer;
	private final AllPlayersCards cards;
	private final Invitation invit;
	private final boolean canCallAnyTarock;
	private final int playerSkarted20;
	
	private PlayerPairs playerPairs = null;
	private boolean isSoloIntentional;
	private Invitation invitAccepted = null;
	private int playerToAnnounceSolo = -1;
	
	public Calling(AllPlayersCards cards, int callerPlayer, Bidding.Invitation invit, int[] tarockSkartedCounts, int playerSkarted20)
	{
		this.callerPlayer = callerPlayer;
		this.cards = cards;
		this.invit = invit;
		
		boolean canCallAnyTarock = false;
		for (int i = 0; i < 4; i++)
		{
			if (i != callerPlayer && tarockSkartedCounts[i] > 0)
			{
				canCallAnyTarock = true;
			}
		}
		this.canCallAnyTarock = canCallAnyTarock;
		
		this.playerSkarted20 = playerSkarted20;
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
		
		isSoloIntentional = calledPlayer == callerPlayer;
		
		//if the player called a card that had been skarted
		if (calledPlayer < 0)
		{
			calledPlayer = callerPlayer;
			
			if (card.equals(new TarockCard(20)) && playerSkarted20 != callerPlayer)
			{
				if (playerSkarted20 < 0)
					throw new RuntimeException();
				playerToAnnounceSolo = playerSkarted20;
			}
		}
		
		playerPairs = new PlayerPairs(callerPlayer, calledPlayer);
		
		invitAccepted = Invitation.NONE;
		if (invit == Invitation.XVIII && card.equals(new TarockCard(18)))
		{
			invitAccepted = invit;
		}
		if (invit == Invitation.XIX && card.equals(new TarockCard(19)))
		{
			invitAccepted = invit;
		}
		
		return true;
	}
	
	public List<Card> getCallableCards()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		Set<Card> callOptions = new LinkedHashSet<Card>();
		
		if (invit == Invitation.XIX)
		{
			Card c = new TarockCard(19);
			callOptions.add(c);
		}
		
		if (invit == Invitation.XVIII)
		{
			Card c = new TarockCard(18);
			callOptions.add(c);
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
		}
		else
		{
			callOptions.addAll(pc.filter(new CallableCardFilter()));
		}
		
		return new ArrayList<Card>(callOptions);
	}
	
	public PlayerPairs getPlayerPairs()
	{
		if (!isFinished())
			throw new IllegalStateException();
		return playerPairs;
	}
	
	public boolean isSoloIntentional()
	{
		if (!isFinished())
			throw new IllegalStateException();
		return isSoloIntentional;
	}

	public Invitation getInvitationAccepted()
	{
		if (!isFinished())
			throw new IllegalStateException();
		return invitAccepted;
	}
	
	public int getPlayerToAnnounceSolo()
	{
		if (!isFinished())
			throw new IllegalStateException();
		return playerToAnnounceSolo;
	}

	public boolean isFinished()
	{
		return playerPairs != null;
	}
}
