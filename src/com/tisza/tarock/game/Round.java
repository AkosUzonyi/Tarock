package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Round
{
	private final int beginnerPlayer;
	private int nextPlayer;
	private int winnerPlayer = -1;
	private Card[] cards = new Card[4];
	
	public Round(int bp)
	{
		beginnerPlayer = bp;
		nextPlayer = beginnerPlayer;
	}
	
	public Card getFirstCard()
	{
		return cards[beginnerPlayer];
	}
	
	public List<Card> getCards()
	{
		return new ArrayList<Card>(Arrays.asList(cards));
	}
	
	public boolean isFinished()
	{
		return winnerPlayer >= 0;
	}
	
	public int getWinner()
	{
		if (!isFinished())
		{
			throw new IllegalStateException("Round has not finished");
		}
		else
		{
			return winnerPlayer;
		}
	}
	
	public int getNextPlayer()
	{
		return nextPlayer;
	}
	
	public void placeCard(Card card)
	{
		cards[nextPlayer++] = card;
		nextPlayer %= 4;
		
		if (nextPlayer == beginnerPlayer)
		{
			Card winnerCard = null;
			int winnerPlayerLocal = -1;
			for (int i = 0; i < cards.length; i++)
			{
				Card c = cards[i];
				if (winnerCard == null || c.doesBeat(winnerCard))
				{
					winnerPlayerLocal = i;
					winnerCard = c;
				}
			}
			
			if (winnerPlayerLocal < 0) throw new Error();
			
			winnerPlayer = winnerPlayerLocal;
		}
	}
}
