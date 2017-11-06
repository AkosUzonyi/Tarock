package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.*;
import com.tisza.tarock.message.event.*;

import java.util.*;

public class Calling extends Phase
{
	private int callerPlayer;
	private boolean canCallAnyTarock;
	
	public Calling(GameState gs)
	{
		super(gs);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CALLING;
	}

	public void onStart()
	{
		callerPlayer = gameState.getBidWinnerPlayer();
		
		canCallAnyTarock = false;
		for (Card c : gameState.getSkartForTeam(Team.OPPONENT))
		{
			if (c instanceof TarockCard)
			{
				canCallAnyTarock = true;
				break;
			}
		}
		
		gameState.sendEvent(callerPlayer, new EventAvailableCalls(getCallableCards()));
		gameState.broadcastEvent(new EventTurn(callerPlayer));
	}
	
	public void call(int player, Card card)
	{
		if (player != callerPlayer)
			return;
		
		if (!getCallableCards().contains(card))
			return;
		
		int calledPlayer = -1;
		for (int i = 0; i < 4; i++)
		{
			PlayerCards pc = gameState.getPlayerCards(i);
			if (pc.hasCard(card))
			{
				calledPlayer = i;
			}
		}
		
		gameState.setSoloIntentional(calledPlayer == callerPlayer);
		
		//if the player called a card that had been skarted
		if (calledPlayer < 0)
		{
			calledPlayer = callerPlayer;
			
			if (card.equals(new TarockCard(20)) && gameState.getPlayerSkarted20() != callerPlayer)
			{
				if (gameState.getPlayerSkarted20() < 0)
					throw new RuntimeException();
				gameState.setPlayerToAnnounceSolo(gameState.getPlayerSkarted20());
			}
		}
		
		gameState.setPlayerPairs(new PlayerPairs(callerPlayer, calledPlayer));
		
		if (gameState.getInvitSent() == Invitation.XVIII && card.equals(new TarockCard(18)) || gameState.getInvitSent() == Invitation.XIX && card.equals(new TarockCard(19)))
		{
			gameState.invitAccepted();
		}
		
		gameState.broadcastEvent(new EventCall(callerPlayer, card));
		gameState.changePhase(new Announcing(gameState));
	}
	
	private List<Card> getCallableCards()
	{
		Set<Card> callOptions = new LinkedHashSet<Card>();
		
		if (gameState.getInvitSent() == Invitation.XIX)
		{
			Card c = new TarockCard(19);
			callOptions.add(c);
		}
		
		if (gameState.getInvitSent() == Invitation.XVIII)
		{
			Card c = new TarockCard(18);
			callOptions.add(c);
		}
		
		PlayerCards pc = gameState.getPlayerCards(callerPlayer);
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
}
