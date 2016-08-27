package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Gameplay
{
	private List<PlayerStat> playerStats = new ArrayList<PlayerStat>();
	
	private List<PlayerCards> playerHands;
	private List<Round> roundsPassed = new ArrayList<Round>();
	private final int beginnerPlayer;
	private Round currentRound;
	
	public Gameplay(List<PlayerCards> ph, int bp)
	{
		if (ph.size() != 4) throw new IllegalArgumentException();
		
		playerHands = new ArrayList<PlayerCards>(ph);
		beginnerPlayer = bp;
		currentRound = new Round(beginnerPlayer);
		
		for (PlayerCards hand : playerHands)
		{
			PlayerStat ps = new PlayerStat();
			ps.initialCards = hand;
			playerStats.add(ps);
		}
	}
	
	public PlaceResult placeCard(Card c)
	{
		if (isFinished())
			throw new IllegalStateException("Game has finished, no cards can be placed");
		
		PlayerCards currentHand = playerHands.get(getNextPlayer());
		
		if (!currentHand.hasCard(c))
			return PlaceResult.NO_SUCH_CARD;
		if (!currentHand.getPlaceableCards(currentRound.getFirstCard()).contains(c))
			return PlaceResult.INVALID_CARD;
		
		currentRound.placeCard(c);
		
		if (currentRound.isFinished())
		{
			roundsPassed.add(currentRound);
			int winner = currentRound.getWinner();
			playerStats.get(winner).cardsWon.addAll(currentRound.getCards());
			currentRound = roundsPassed.size() >= 9 ? null : new Round(winner);
		}
		
		return PlaceResult.OK;
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
	
	public List<PlayerStat> getPlayerStats()
	{
		if (!isFinished())
			throw new IllegalStateException("Game is in progress");
		return playerStats;
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
	
	public static enum PlaceResult
	{
		OK, NO_SUCH_CARD, INVALID_CARD, NOT_PLAYERS_TURN;
	}
	
	public static class PlayerStat
	{
		public PlayerCards initialCards;
		public List<Card> cardsWon = new ArrayList<Card>();
	}
}
