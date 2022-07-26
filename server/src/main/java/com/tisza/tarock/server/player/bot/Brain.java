package com.tisza.tarock.server.player.bot;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.announcement.*;
import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.card.filter.*;
import com.tisza.tarock.server.player.bot.figure.*;
import com.tisza.tarock.server.player.bot.figure.paskievics.*;
import com.tisza.tarock.server.player.bot.figure.paskievics.Trull;
import com.tisza.tarock.server.player.bot.figure.paskievics.Volat;

import java.util.*;
import java.util.stream.*;

public class Brain
{
	private Memory memory;
	private final Personality personality;
	private final Set<Figure> figures = new HashSet<>();

	public Brain(Personality personality)
	{
		this.personality = personality;
	}

	public void announce(PlayerSeat player, AnnouncementContra announcement)
	{
		memory.addAnnouncement(player, announcement);
	}

	public void bid(PlayerSeat player, int bid)
	{
		memory.getBids().put(player, bid);
	}

	public void call(PlayerSeat player, Card card)
	{
		memory.setCaller(player);
		memory.setCalledCard(card);
	}

	public void playCard(PlayerSeat player, Card card, boolean myCard)
	{
		memory.addPlayedCard(player, card);

		if (myCard)
		{
			memory.getMyCards().removeCard(card);
		}

		if (memory.getCardsInTrick() == 0)
		{
			memory.setCurrentFirstCard(card);
			memory.setCurrentStrongestCard(card);
		}
		else if (card.doesBeat(memory.getCurrentStrongestCard()))
		{
			memory.setCurrentStrongestCard(card);
		}

		memory.updateCardsInTrick();
	}

	@SuppressWarnings("unchecked")
	public List<Card> fold()
	{
		final Map<Card, Integer> cards = new HashMap<>();
		figures.forEach(figure -> figure.fold(memory.getMyCards().filter(new SkartableCardFilter(memory.getGameType())))
				.forEach((key, value) ->
				{
					cards.putIfAbsent(key, 0);
					cards.put(key, cards.get(key) + value);
				}));

		final List<Map.Entry<Card, Integer>> cardsList = new ArrayList<>(cards.entrySet());
		cardsList.sort((Comparator<? super Map.Entry<Card, Integer>>) Map.Entry.comparingByValue().reversed());
		return cardsList.stream().map(Map.Entry::getKey).collect(Collectors.toList())
				.subList(0, memory.getMyCards().size() - 9);
	}

	@SuppressWarnings("unchecked")
	public Card turn()
	{
		final Map<Card, Integer> cards = new HashMap<>();
		figures.forEach(figure -> figure.play(memory.getMyCards().getPlayableCards(memory.getCurrentFirstCard()))
				.forEach((key, value) ->
				{
					cards.putIfAbsent(key, 0);
					cards.put(key, cards.get(key) + value);
				}));

		final List<Map.Entry<Card, Integer>> cardsList = new ArrayList<>(cards.entrySet());
		cardsList.sort((Comparator<? super Map.Entry<Card, Integer>>) Map.Entry.comparingByValue().reversed());
		return cardsList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).get(0);
	}

	public void startGame(GameType gameType, int beginnerPlayer)
	{
		memory = new Memory();
		memory.setGameType(gameType);
		memory.setBeginnerPlayer(beginnerPlayer);
		switch (gameType)
		{ //TODO add figures
			case ZEBI:
			case MAGAS:
			case ILLUSZTRALT:
			case PASKIEVICS:
				figures.add(new Game(memory, personality));
				figures.add(new DoubleGame(memory,personality));
				figures.add(new Volat(memory,personality));
				figures.add(new EightTarock(memory,personality));
				figures.add(new NineTarock(memory,personality));
				figures.add(new FourKing(memory,personality));
				figures.add(new Trull(memory,personality));
				figures.add(new PagatUltimo(memory,personality));
				figures.add(new XXICatch(memory,personality));
		}
	}

	public boolean playerCards(PlayerCards cards, boolean canBeThrown)
	{
		memory.setMyCards(cards);
		if (canBeThrown)
		{
			switch (personality.getKeepCardsWhenWeak())
			{
				case NONE:
					return true;
				case RARE:
					if (missingSuperiorTarocks() < 1)
					{
						return true;
					}
					break;
				case FEW:
					if (missingSuperiorTarocks() < 2)
					{
						return true;
					}
					break;
				case OFTEN:
					if (missingSuperiorTarocks() < 3)
					{
						return true;
					}
					break;
				case ALWAYS:
					if (missingSuperiorTarocks() < 4)
					{
						return true;
					}
					break;
			}
		}
		return false;
	}

	public int availableBids(Collection<Integer> bids)
	{
		if (bids.size() == 1)
		{
			return bids.iterator().next();
		}
		if (!memory.getMyCards().hasCard(Card.getTarockCard(21)) && !memory.getMyCards().hasCard(Card.getTarockCard(22)))
		{
			switch (personality.getBidWithPagat())
			{
				case NONE:
					return -1;
				case RARE:
					if (missingSuperiorTarocks() < 1)
					{
						return internalBid(bids);
					}
				case FEW:
					if (missingSuperiorTarocks() < 2)
					{
						return internalBid(bids);
					}
				case OFTEN:
					if (missingSuperiorTarocks() < 3)
					{
						return internalBid(bids);
					}
				case ALWAYS:
					if (missingSuperiorTarocks() < 4)
					{
						return internalBid(bids);
					}
			}
			return -1;
		}
		return internalBid(bids);
	}

	public Card availableCalls(Collection<Card> cards)
	{
		if (memory.getMyCards().hasCard(Card.getTarockCard(20)))
		{
			switch (personality.getSelfCalling())
			{
				case NONE:
					break;
				case RARE:
					if (missingSuperiorTarocks() < 1)
					{
						return Card.getTarockCard(20);
					}
					break;
				case FEW:
					if (missingSuperiorTarocks() < 2)
					{
						return Card.getTarockCard(20);
					}
					break;
				case OFTEN:
					if (missingSuperiorTarocks() < 3)
					{
						return Card.getTarockCard(20);
					}
					break;
				case ALWAYS:
					if (missingSuperiorTarocks() < 4)
					{
						return Card.getTarockCard(20);
					}
					break;
			}
		}

		List<Card> sortedCards = cards.stream().sorted(Comparator.comparingInt(this::getCardValue).reversed())
				.collect(Collectors.toList());
		for (Card card : sortedCards)
		{
			if (!memory.getMyCards().hasCard(card))
			{
				return card;
			}
		}

		return sortedCards.get(0);
	}

	public void foldTarock(PlayerSeatMap<Integer> foldedTarocks)
	{
		memory.setFoldedTarocks(foldedTarocks);
	}

	public List<AnnouncementContra> availableAnnouncements(List<AnnouncementContra> announcements)
	{
		List<AnnouncementContra> results = new ArrayList<>();
		if (announcements.contains(new AnnouncementContra(Announcements.hkp, 0)))
			results.add(new AnnouncementContra(Announcements.hkp, 0));

		if (!announcements.isEmpty())
		{
			results.addAll(
					figures.stream().map(a -> a.announce(announcements)).flatMap(List::stream).collect(Collectors.toList()));
		}
		return results;
	}

	private int getCardValue(Card card)
	{
		if (card instanceof SuitCard)
		{
			return ((SuitCard) card).getValue();
		}
		if (card instanceof TarockCard)
		{
			return ((TarockCard) card).getValue();
		}
		return 0;
	}

	private int internalBid(Collection<Integer> bids)
	{
		return bids.stream().sorted(Comparator.comparingInt(b-> (int) b)
				.reversed()).collect(Collectors.toList()).get(0);
	}
	private int missingSuperiorTarocks()
	{ //maximum value: 6
		List<Card> superiorTarocks = Arrays.asList(Card.getTarockCard(20), Card.getTarockCard(19), Card.getTarockCard(18),
				Card.getTarockCard(17), Card.getTarockCard(16), Card.getTarockCard(15));
		superiorTarocks.removeAll(memory.getMyCards().getCards());
		return superiorTarocks.size();
	}
}
