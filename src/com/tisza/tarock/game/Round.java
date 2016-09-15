package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Round
{
	private final int beginnerPlayer;
	private int currentPlayer;
	private int winnerPlayer = -1;
	private Card[] cards = new Card[4];
	private boolean finished = false;
	
	public Round(int bp)
	{
		if (bp < 0 || bp >= 4)
			throw new IllegalArgumentException();
		
		beginnerPlayer = bp;
		currentPlayer = beginnerPlayer;
		winnerPlayer = currentPlayer;
	}
	
	public int getBeginnerPlayer()
	{
		return beginnerPlayer;
	}
	
	public Card getFirstCard()
	{
		return cards[beginnerPlayer];
	}
	
	public List<Card> getCards()
	{
		return Collections.unmodifiableList(Arrays.asList(cards));
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public int getWinner()
	{
		if (!isFinished())
			throw new IllegalStateException("Round has not finished");
		return winnerPlayer;
	}
	
	public int getNextPlayer()
	{
		return currentPlayer;
	}
	
	public void placeCard(Card card)
	{
		if (isFinished())
			throw new IllegalStateException();
		
		cards[currentPlayer] = card;
		
		Card currentWinnerCard = cards[winnerPlayer];
		if (card.doesBeat(currentWinnerCard))
		{
			winnerPlayer = currentPlayer;
		}
		
		currentPlayer++;
		currentPlayer %= 4;
		
		if (currentPlayer == beginnerPlayer)
		{
			finished = true;
		}
	}
}
