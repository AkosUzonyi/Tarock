package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.stream.*;

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
		for (PlayerSeat player : PlayerSeat.getAll())
		{
			if (player == callerPlayer)
				continue;

			for (Card c : game.getSkart(player))
				if (c instanceof TarockCard)
					canCallAnyTarock = true;
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

		//if the player called a card that had been folded
		if (calledPlayer == null)
		{
			if (game.getSkart(game.getBidWinnerPlayer()).contains(card))
				game.setSoloIntentional();

			calledPlayer = callerPlayer;
		}

		game.setCalledCard(card);
		game.setPlayerPairs(new PlayerPairs(callerPlayer, calledPlayer));

		for (PlayerSeat p : PlayerSeat.getAll())
			game.addNewTeamInfo(p, p);

		if (!game.getPlayerPairs().isSolo() || game.isSoloIntentional())
			game.revealAllTeamInfoFor(calledPlayer);

		if (game.getInvitSent() != null && card.equals(game.getInvitSent().getCard()))
		{
			game.invitAccepted();

			if (game.getSkart(callerPlayer).stream().noneMatch(c -> c instanceof TarockCard))
				game.revealAllTeamInfo();
		}

		if (game.getSkart(callerPlayer).contains(card))
			game.revealAllTeamInfo();

		game.broadcastEvent(Event.call(player, card));
		game.changePhase(new Announcing(game));

		return true;
	}

	private void sendAvailableCalls()
	{
		game.sendEvent(callerPlayer, Event.availableCalls(getCallableCards()));
		game.turn(callerPlayer);
	}

	@Override
	public List<Action> getAvailableActions()
	{
		return getCallableCards().stream().map(Action::call).collect(Collectors.toList());
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

		CardFilter cf = new CallableCardFilter();
		if (canCallAnyTarock)
		{
			for (Card c : Card.getAll())
				if (cf.match(c))
					callOptions.add(c);
		}
		else if (game.getGameType().hasParent(GameType.ZEBI))
		{
			callOptions.addAll(pc.filter(cf));
		}
		else
		{
			callOptions.add(TarockCard.getTarockCard(20));
		}
		
		return new ArrayList<>(callOptions);
	}
}
