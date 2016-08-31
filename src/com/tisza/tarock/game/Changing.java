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
	
	public Changing(AllPlayersCards cards, List<Card> talon, int winnerPlayer, int winnerBid)
	{
		this.cardsAfter = cards;
		this.talon = talon;
		this.winnerPlayer = winnerPlayer;
		this.winnerBid = winnerBid;
	}
	
	public Collection<Card> getCardsObtainedFromTalon(int player)
	{
		if (cardsFromTalon == null) dealCardsFromTalon();
		return Collections.unmodifiableCollection(cardsFromTalon.get(player));
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
	
	public boolean skart(int player, Collection<Card> cardsToSkart)
	{
		if (donePlayer[player])
			return false;
		
		List<Card> cardsFromTalonForPlayer = new ArrayList<Card>(cardsFromTalon.get(player));
		PlayerCards pc = cardsAfter.getPlayerCards(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
			return false;
		
		for (Card c : cardsToSkart)
		{
			//TODO
			//if (forbiddenCards.contains(c))
				//return false;
			if (!pc.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
			{
				return false;
			}
		}
		
		for (Card c : cardsToSkart)
		{
			@SuppressWarnings("unused")
			boolean x = pc.removeCard(c) || cardsFromTalonForPlayer.remove(c);
		}
		
		pc.getCards().addAll(cardsFromTalonForPlayer);
		
		if (pc.getCards().size() != 9)
			throw new Error();
		
		donePlayer[player] = true;

		return true;
	}
	
	public AllPlayersCards getCardsAfter()
	{
		return cardsAfter;
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
