package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

class Changing extends Phase
{
	private final SkartableCardFilter cardFilter;
	
	private PlayerSeatMap<Boolean> donePlayer = new PlayerSeatMap<>(false);
	private PlayerSeatMap<Integer> tarockCounts = new PlayerSeatMap<>();
	
	public Changing(Game game)
	{
		super(game);
		cardFilter = new SkartableCardFilter(game.getGameType());
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.CHANGING;
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
			game.sendEvent(player, Event.playerCards(playerCards.clone()));
			game.sendEvent(player, Event.turn(player));
			if (cardsFromTalon.isEmpty())
				change(player, Collections.EMPTY_LIST);
			cardsFromTalon.clear();

			player = player.nextPlayer();
		}
	}
	
	@Override
	public boolean change(PlayerSeat player, List<Card> cardsToSkart)
	{
		if (donePlayer.get(player))
			return false;
		
		PlayerCards skartingPlayerCards = game.getPlayerCards(player);

		if (skartingPlayerCards.size() - cardsToSkart.size() != Game.ROUND_COUNT)
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

			if (!skartingPlayerCards.hasCard(c))
				return false;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
		{
			if (c instanceof TarockCard)
			{
				tarockCount++;
				if (c.equals(Card.getTarockCard(20)))
				{
					game.setPlayerSkarted20(player);
				}
			}

			game.addCardToSkart(player == game.getBidWinnerPlayer() ? Team.CALLER : Team.OPPONENT, c);
		}
		tarockCounts.put(player, tarockCount);
		
		skartingPlayerCards.removeCards(cardsToSkart);
		donePlayer.put(player, true);
		game.sendEvent(player, Event.playerCards(skartingPlayerCards.clone()));
		game.broadcastEvent(Event.changeDone(player));

		if (isFinished())
		{
			game.broadcastEvent(Event.skartTarock(tarockCounts));
			game.changePhase(new Calling(game));
		}

		return true;
	}
	
	@Override
	public boolean throwCards(PlayerSeat player)
	{
		if (donePlayer.get(player))
			return false;

		if (!game.getPlayerCards(player).canBeThrown())
			return false;

		game.broadcastEvent(Event.throwCards(player));
		game.changePhase(new PendingNewGame(game, true));

		return true;
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
