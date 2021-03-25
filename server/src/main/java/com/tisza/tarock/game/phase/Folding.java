package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.message.*;

import java.util.*;
import java.util.stream.*;

class Folding extends Phase
{
	private final SkartableCardFilter cardFilter;
	
	private PlayerSeatMap<Boolean> donePlayer = new PlayerSeatMap<>(false);
	private PlayerSeatMap<Integer> tarockCounts = new PlayerSeatMap<>();
	
	public Folding(Game game)
	{
		super(game);
		cardFilter = new SkartableCardFilter(game.getGameType());
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.FOLDING;
	}
	
	@Override
	public void onStart()
	{
		dealCardsFromTalon();
	}

	private void dealCardsFromTalon()
	{
		List<Card> remainingCards = new LinkedList<>(game.getTalon());
		PlayerSeat player = game.getBidWinnerPlayer();

		game.setAllTurn();

		for (int i = 0; i < 4; i++)
		{
			int cardCount;
			if (player == game.getBidWinnerPlayer())
			{
				cardCount = game.getWinnerBid();
			}
			else
			{
				cardCount = (int)Math.ceil((float)remainingCards.size() / (4 - i));
			}

			List<Card> cardsFromTalon = remainingCards.subList(0, cardCount);
			PlayerCards playerCards = game.getPlayerCards(player);
			playerCards.addCards(cardsFromTalon);
			game.sendEvent(player, Event.playerCards(playerCards.clone(), canThrowCards(player)));
			game.sendEvent(player, Event.turn(player));
			if (cardsFromTalon.isEmpty())
				fold(player, Collections.emptyList());
			cardsFromTalon.clear();

			player = player.nextPlayer();
		}

		if (game.getPlayerCards(game.getBidWinnerPlayer()).getCards().stream().noneMatch(Card::isHonor))
		{
			game.broadcastEvent(Event.throwCards(player));
			game.changePhase(new PendingNewGame(game, true));
		}
	}
	
	@Override
	public boolean fold(PlayerSeat player, List<Card> cardsToSkart)
	{
		if (donePlayer.get(player))
			return false;
		
		PlayerCards foldingPlayerCards = game.getPlayerCards(player);

		if (foldingPlayerCards.size() - cardsToSkart.size() != Game.ROUND_COUNT)
		{
			//game.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return false;
		}
		
		Set<Card> checkedSkartCards = new HashSet<>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				//game.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
				return false;
			}
			
			if (!checkedSkartCards.add(c))
				return false;

			if (!foldingPlayerCards.hasCard(c))
				return false;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
			if (c instanceof TarockCard)
				tarockCount++;

		game.setSkart(player, cardsToSkart);
		tarockCounts.put(player, tarockCount);
		
		foldingPlayerCards.removeCards(cardsToSkart);
		donePlayer.put(player, true);
		game.setTurnOf(player, false);
		game.sendEvent(player, Event.playerCards(foldingPlayerCards.clone(), false));
		game.broadcastEvent(Event.foldDone(player));

		if (isFinished())
		{
			game.broadcastEvent(Event.foldTarock(tarockCounts));

			List<Card> callerSkartedTarocks = game.getSkart(game.getBidWinnerPlayer()).stream().filter(c -> c instanceof TarockCard).collect(Collectors.toList());
			if (!callerSkartedTarocks.isEmpty())
				game.broadcastEvent(Event.fold(game.getBidWinnerPlayer(), callerSkartedTarocks));

			game.changePhase(new Calling(game));
		}

		return true;
	}

	@Override
	public boolean canThrowCards(PlayerSeat player)
	{
		if (donePlayer.get(player))
			return false;

		boolean lastPlayerThrow = game.getWinnerBid() == 3 && game.getBidWinnerPlayer() == player && player == PlayerSeat.SEAT3;
		return game.getPlayerCards(player).canBeThrown(game.getGameType()) || lastPlayerThrow;
	}

	private boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}
}
