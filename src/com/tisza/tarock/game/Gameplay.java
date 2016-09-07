package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Gameplay
{
	private List<List<Card>> wonCards = new ArrayList<List<Card>>();
	
	private AllPlayersCards cards;
	private List<Round> roundsPassed = new ArrayList<Round>();
	private final int beginnerPlayer;
	private Round currentRound;
	
	public Gameplay(AllPlayersCards cards, int bp)
	{
		this.cards = cards.clone();
		beginnerPlayer = bp;
		currentRound = new Round(beginnerPlayer);
		for (int i = 0; i < 4; i++)
		{
			wonCards.add(new ArrayList<Card>());
		}
	}
	
	public boolean playCard(Card c, int player)
	{
		if (isFinished())
			return false;
		
		if (player != getNextPlayer())
			return false;
		
		if (!getPlaceableCards().contains(c))
			return false;
		
		cards.getPlayerCards(player).removeCard(c);
		currentRound.placeCard(c);
		
		if (currentRound.isFinished())
		{
			roundsPassed.add(currentRound);
			int winner = currentRound.getWinner();
			wonCards.get(winner).addAll(currentRound.getCards());
			currentRound = roundsPassed.size() >= 9 ? null : new Round(winner);
		}
		
		return true;
	}
	
	public Collection<Card> getPlaceableCards()
	{
		if (isFinished())
			throw new IllegalStateException("Game has finished");
		
		PlayerCards pc = cards.getPlayerCards(getNextPlayer());
		Card firstCard = currentRound.getFirstCard();
		return Collections.unmodifiableCollection(pc.getPlaceableCards(firstCard));
	}
	
	public boolean isFinished()
	{
		return currentRound == null;
	}
	
	public int getBeginnerPlayer()
	{
		if (!isFinished())
			throw new IllegalStateException("Game is in progress");
		return beginnerPlayer;
	}
	
	public Collection<Card> getWonCards(int player)
	{
		if (!isFinished())
			throw new IllegalStateException("Game is in progress");
		return wonCards.get(player);
	}

	public List<Round> getRoundsPassed()
	{
		if (!isFinished())
			throw new IllegalStateException("Game is in progress");
		return roundsPassed;
	}

	public int getNextPlayer()
	{
		if (isFinished())
			throw new IllegalStateException("Game has finished, no one is the next");
		return currentRound.getNextPlayer();
	}
}
