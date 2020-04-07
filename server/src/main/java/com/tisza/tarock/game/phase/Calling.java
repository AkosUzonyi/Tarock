package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.message.*;

import java.util.*;

class Calling extends Phase
{
	private PlayerSeat callerPlayer;
	private boolean canCallAnyTarock;
	
	public Calling(Game game)
	{
		super(game);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CALLING;
	}

	@Override
	public void onStart()
	{
		callerPlayer = game.getBidWinnerPlayer();
		
		canCallAnyTarock = false;
		for (Card c : game.getSkartForTeam(Team.OPPONENT))
		{
			if (c instanceof TarockCard)
			{
				canCallAnyTarock = true;
				break;
			}
		}

		sendAvailableCalls();
	}

	@Override
	public boolean call(PlayerSeat player, Card card)
	{
		if (player != callerPlayer)
			return false;
		
		if (!getCallableCards().contains(card))
			return false;

		PlayerSeat calledPlayer = null;
		for (PlayerSeat p : PlayerSeat.getAll())
		{
			PlayerCards pc = game.getPlayerCards(p);
			if (pc.hasCard(card))
			{
				calledPlayer = p;
			}
		}

		if (calledPlayer == callerPlayer)
			game.setSoloIntentional();

		//if the player called a card that had been skarted
		if (calledPlayer == null)
		{
			if (game.getSkartForTeam(Team.CALLER).contains(card))
				game.setSoloIntentional();

			calledPlayer = callerPlayer;
			
			if (card.equals(Card.getTarockCard(20)) && game.getPlayerSkarted20() != callerPlayer)
			{
				if (game.getPlayerSkarted20() == null)
					throw new RuntimeException();
				game.setPlayerToAnnounceSolo(game.getPlayerSkarted20());
			}
		}

		game.setPlayerPairs(new PlayerPairs(callerPlayer, calledPlayer));

		Invitation invit = game.getInvitSent();
		if (invit != null && card.equals(invit.getCard()))
		{
			game.invitAccepted();
		}

		game.broadcastEvent(Event.call(player, card));
		game.changePhase(new Announcing(game));

		return true;
	}

	private void sendAvailableCalls()
	{
		game.sendEvent(callerPlayer, Event.availableCalls(getCallableCards()));
		game.broadcastEvent(Event.turn(callerPlayer));
	}

	private List<Card> getCallableCards()
	{
		Set<Card> callOptions = new LinkedHashSet<>();

		Invitation invit = game.getInvitSent();
		if (invit != null && game.getInvitingPlayer() != callerPlayer)
			callOptions.add(invit.getCard());

		PlayerCards pc = game.getPlayerCards(callerPlayer);
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
		
		return new ArrayList<>(callOptions);
	}
}
