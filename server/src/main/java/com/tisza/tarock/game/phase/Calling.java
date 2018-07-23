package com.tisza.tarock.game.phase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.phase.Bidding.*;

import java.util.*;

class Calling extends Phase
{
	private PlayerSeat callerPlayer;
	private boolean canCallAnyTarock;
	
	public Calling(GameState game)
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
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		game.getPlayerEventSender(player).turn(callerPlayer);
		if (player == callerPlayer)
			sendAvailableCalls();
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
		if (invit != Invitation.NONE && card.equals(invit.getCard()))
		{
			game.invitAccepted();
		}

		history.setCalledCard(player, card);
		game.getBroadcastEventSender().call(player, card);
		game.changePhase(new Announcing(game));
	}

	private void sendAvailableCalls()
	{
		game.getPlayerEventSender(callerPlayer).availabeCalls(getCallableCards());
		game.getBroadcastEventSender().turn(callerPlayer);
	}

	private List<Card> getCallableCards()
	{
		Set<Card> callOptions = new LinkedHashSet<>();

		Invitation invit = game.getInvitSent();
		if (invit != Invitation.NONE)
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
