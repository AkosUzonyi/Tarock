package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.card.filter.CallableCardFilter;
import com.tisza.tarock.card.filter.CardFilter;
import com.tisza.tarock.game.Bidding.Invitation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
		
		gameState.getEventQueue().toPlayer(callerPlayer).availabeCalls(getCallableCards());
		gameState.getEventQueue().broadcast().turn(callerPlayer);
	}
	
	public boolean call(int player, Card card)
	{
		if (player != callerPlayer)
			return false;
		
		if (!getCallableCards().contains(card))
			return false;
		
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
		
		gameState.changePhase(new Announcing(gameState));

		return true;
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
