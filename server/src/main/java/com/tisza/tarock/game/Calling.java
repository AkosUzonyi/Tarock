package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.Bidding.*;

import java.util.*;

class Calling extends Phase
{
	private PlayerSeat callerPlayer;
	private boolean canCallAnyTarock;
	
	public Calling(GameSession gameSession)
	{
		super(gameSession);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CALLING;
	}

	@Override
	public void onStart()
	{
		callerPlayer = currentGame.getBidWinnerPlayer();
		
		canCallAnyTarock = false;
		for (Card c : currentGame.getSkartForTeam(Team.OPPONENT))
		{
			if (c instanceof TarockCard)
			{
				canCallAnyTarock = true;
				break;
			}
		}
		
		gameSession.getPlayerEventQueue(callerPlayer).availabeCalls(getCallableCards());
		gameSession.getBroadcastEventSender().turn(callerPlayer);
	}
	
	@Override
	public void call(PlayerSeat player, Card card)
	{
		if (player != callerPlayer)
			return;
		
		if (!getCallableCards().contains(card))
			return;

		PlayerSeat calledPlayer = null;
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			PlayerCards pc = currentGame.getPlayerCards(p);
			if (pc.hasCard(card))
			{
				calledPlayer = p;
			}
		}
		
		currentGame.setSoloIntentional(calledPlayer == callerPlayer);
		
		//if the player called a card that had been skarted
		if (calledPlayer == null)
		{
			calledPlayer = callerPlayer;
			
			if (card.equals(Card.getTarockCard(20)) && currentGame.getPlayerSkarted20() != callerPlayer)
			{
				if (currentGame.getPlayerSkarted20() == null)
					throw new RuntimeException();
				currentGame.setPlayerToAnnounceSolo(currentGame.getPlayerSkarted20());
			}
		}

		currentGame.setPlayerPairs(new PlayerPairs(callerPlayer, calledPlayer));

		Invitation invit = currentGame.getInvitSent();
		if (invit != Invitation.NONE && card.equals(invit.getCard()))
		{
			currentGame.invitAccepted();
		}

		gameSession.getBroadcastEventSender().call(player, card);
		gameSession.changePhase(new Announcing(gameSession));
	}
	
	private List<Card> getCallableCards()
	{
		Set<Card> callOptions = new LinkedHashSet<Card>();
		
		if (currentGame.getInvitSent() == Invitation.XIX)
		{
			Card c = Card.getTarockCard(19);
			callOptions.add(c);
		}
		
		if (currentGame.getInvitSent() == Invitation.XVIII)
		{
			Card c = Card.getTarockCard(18);
			callOptions.add(c);
		}
		
		PlayerCards pc = currentGame.getPlayerCards(callerPlayer);
		for (int t = 20; t >= 1; t--)
		{
			TarockCard c = Card.getTarockCard(t);
			if (!pc.hasCard(c))
			{
				callOptions.add(c);
				break;
			}
		}
		
		if (canCallAnyTarock)
		{
			CardFilter cf = new CallableCardFilter();
			for (Card c : Card.getAll())
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
