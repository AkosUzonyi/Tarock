package com.tisza.tarock.game;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;

import java.util.*;

class Changing extends Phase
{
	private final SkartableCardFilter cardFilter;
	
	private PlayerSeat.Map<Boolean> donePlayer = new PlayerSeat.Map<>(false);
	private PlayerSeat.Map<Integer> tarockCounts = new PlayerSeat.Map<>();
	
	public Changing(GameState game)
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

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		for (PlayerSeat otherPlayer : PlayerSeat.getAll())
		{
			if (donePlayer.get(otherPlayer))
			{
				game.getPlayerEventSender(player).changeDone(otherPlayer);
			}
		}

		if (!donePlayer.get(player))
			game.getPlayerEventSender(player).turn(player);
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
			game.getPlayerEventSender(player).playerCards(playerCards);
			game.getPlayerEventSender(player).turn(player);
			history.setCardsFromTalon(player, new ArrayList<>(cardsFromTalon));
			if (cardsFromTalon.isEmpty())
				change(player, Collections.EMPTY_LIST);
			cardsFromTalon.clear();

			player = player.nextPlayer();
		}
	}
	
	@Override
	public void change(PlayerSeat player, List<Card> cardsToSkart)
	{
		if (donePlayer.get(player))
			return;
		
		PlayerCards skartingPlayerCards = game.getPlayerCards(player);

		if (skartingPlayerCards.size() - cardsToSkart.size() != GameState.ROUND_COUNT)
		{
			//game.sendEvent(player, new EventActionFailed(Reason.WRONG_SKART_COUNT));
			return;
		}
		
		Set<Card> checkedSkartCards = new HashSet<>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
			{
				//game.sendEvent(player, new EventActionFailed(Reason.INVALID_SKART));
				return;
			}
			
			if (!checkedSkartCards.add(c))
				return;

			if (!skartingPlayerCards.hasCard(c))
				return;
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
		history.setCardsSkarted(player, cardsToSkart);
		game.getPlayerEventSender(player).playerCards(skartingPlayerCards);
		game.getBroadcastEventSender().changeDone(player);

		if (isFinished())
		{
			game.getBroadcastEventSender().skartTarock(tarockCounts);
			game.changePhase(new Calling(game));
		}
	}
	
	@Override
	public void throwCards(PlayerSeat player)
	{
		if (donePlayer.get(player))
			return;

		if (!game.getPlayerCards(player).canBeThrown())
			return;

		game.getBroadcastEventSender().throwCards(player);
		game.changePhase(new PendingNewGame(game, true));
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
