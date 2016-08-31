package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Changing
{
	private static final List<Card> forbiddenCards = new ArrayList<Card>();
	
	private AllPlayersCards cardsAfter;
	private List<Card> talon;
	private int winnerPlayer;
	private int winnerBid;	
	
	private List<List<Card>> cardsFromTalon = null;
	private boolean[] donePlayer = new boolean[4];
	private int[] tarockCounts = new int[4];
	
	public Changing(AllPlayersCards cards, List<Card> talon, int winnerPlayer, int winnerBid)
	{
		this.cardsAfter = cards;
		this.talon = talon;
		this.winnerPlayer = winnerPlayer;
		this.winnerBid = winnerBid;
	}
	
	public List<Card> getCardsObtainedFromTalon(int player)
	{
		if (cardsFromTalon == null) dealCardsFromTalon();
		return Collections.unmodifiableList(cardsFromTalon.get(player));
	}
	
	private void dealCardsFromTalon()
	{
		cardsFromTalon = new ArrayList<List<Card>>(4);
		
		for (int i = 0; i < 4; i++)
		{
			cardsFromTalon.add(new ArrayList<Card>());
		}
		
		List<Card> cardsRemaining = new LinkedList<Card>(talon);
		for (int i = 0; i < 4; i++)
		{
			int player = (winnerPlayer + i) % 4;
			
			int cardCount;
			if (player == winnerPlayer)
			{
				cardCount = winnerBid;
			}
			else
			{
				cardCount = (int)Math.ceil((float)cardsRemaining.size() / (4 - i));
			}
			
			for (int j = 0; j < cardCount; j++)
			{
				cardsFromTalon.get(player).add(cardsRemaining.remove(0));
			}
		}
	}
	
	public boolean skart(int player, List<Card> cardsToSkart)
	{
		if (donePlayer[player])
			return false;
		
		List<Card> cardsFromTalonForPlayer = new ArrayList<Card>(cardsFromTalon.get(player));
		PlayerCards pc = cardsAfter.getPlayerCards(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
			return false;
		
		List<Card> cardsToSkartClone = new ArrayList<Card>(cardsToSkart);
		while (!cardsToSkartClone.isEmpty())
		{
			Card c = cardsToSkartClone.remove(0);
			
			//TODO
			//if (forbiddenCards.contains(c))
			//	return false;
			
			if (cardsToSkartClone.contains(c))
				return false;
			
			if (!pc.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
				return false;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
		{
			if (c instanceof TarockCard)
			{
				tarockCount++;
			}
		}
		tarockCounts[player] = tarockCount;
		
		for (Card c : cardsToSkart)
		{
			pc.removeCard(c);
			cardsFromTalonForPlayer.remove(c);
		}
		
		pc.getCards().addAll(cardsFromTalonForPlayer);
		
		donePlayer[player] = true;

		return true;
	}
	
	public AllPlayersCards getCardsAfter()
	{
		return cardsAfter;
	}
	
	public int[] getTarockCounts()
	{
		return tarockCounts.clone();
	}
	
	public boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}

	static
	{
		for (int s = 0; s < 4; s++)
		{
			forbiddenCards.add(new SuitCard(s, 5));
		}
		forbiddenCards.add(new TarockCard(1));
		forbiddenCards.add(new TarockCard(2));
		forbiddenCards.add(new TarockCard(21));
		forbiddenCards.add(new TarockCard(22));
	}
}
