package com.tisza.tarock.game;

import com.tisza.tarock.card.*;

import java.util.*;

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
	
	public int getCurrentPlayer()
	{
		return currentPlayer;
	}
	
	public int getWinner()
	{
		if (!isFinished())
			throw new IllegalStateException("Round has not finished");
		return winnerPlayer;
	}
	
	public Card getFirstCard()
	{
		return getCardByIndex(0);
	}
	
	public Card getCardByIndex(int n)
	{
		return cards[(beginnerPlayer + n) % 4];
	}
	
	public Card getCardByPlayer(int player)
	{
		return cards[player];
	}
	
	public int getPlayerOfCard(Card card)
	{
		for (int player = 0; player < 4; player++)
		{
			if (cards[player].equals(card))
			{
				return player;
			}
		}
		return -1;
	}

	public Collection<Card> getCards()
	{
		return Arrays.asList(cards);
	}
	
	public boolean isFinished()
	{
		return finished;
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
