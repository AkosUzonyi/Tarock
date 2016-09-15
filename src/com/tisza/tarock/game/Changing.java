package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.card.filter.*;

public class Changing
{
	private static final SkartableCardFilter cardFilter = new SkartableCardFilter();
	
	private final List<Card> talon;
	private final int winnerPlayer;
	private final int winnerBid;
	
	private AllPlayersCards cardsAfter;
	private Map<Team, List<Card>> skartForTeams = new HashMap<Team, List<Card>>();
	
	private List<List<Card>> cardsFromTalon = null;
	private boolean[] donePlayer = new boolean[4];
	private int[] tarockCounts = new int[4];
	
	public Changing(AllPlayersCards cards, List<Card> talon, int winnerPlayer, int winnerBid)
	{
		this.cardsAfter = cards.clone();
		this.talon = talon;
		this.winnerPlayer = winnerPlayer;
		this.winnerBid = winnerBid;
		
		for (Team t : Team.values())
		{
			skartForTeams.put(t, new ArrayList<Card>());
		}
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
		
		PlayerCards skartingPlayerCards = cardsAfter.getPlayerCards(player);
		List<Card> cardsFromTalonForPlayer = cardsFromTalon.get(player);
		
		if (cardsToSkart.size() != cardsFromTalonForPlayer.size())
			return false;
		
		List<Card> reviewedSkartCards = new ArrayList<Card>();
		for (Card c : cardsToSkart)
		{
			if (!cardFilter.match(c))
				return false;
			
			if (reviewedSkartCards.contains(c))
				return false;
			
			reviewedSkartCards.add(c);
			
			if (!skartingPlayerCards.hasCard(c) && !cardsFromTalonForPlayer.contains(c))
				return false;
		}
		
		int tarockCount = 0;
		for (Card c : cardsToSkart)
		{
			if (c instanceof TarockCard)
			{
				tarockCount++;
			}
			
			skartForTeams.get(player == winnerPlayer ? Team.CALLER : Team.OPPONENT).add(c);
		}
		tarockCounts[player] = tarockCount;
		
		skartingPlayerCards.getCards().addAll(cardsFromTalonForPlayer);
		skartingPlayerCards.getCards().removeAll(cardsToSkart);
		
		donePlayer[player] = true;

		return true;
	}
	
	public AllPlayersCards getCardsAfter()
	{
		return cardsAfter;
	}
	
	public List<Card> getSkartForTeam(Team t)
	{
		return skartForTeams.get(t);
	}
	
	public int[] getTarockCounts()
	{
		return tarockCounts;
	}
	
	public boolean isDone(int player)
	{
		return donePlayer[player];
	}
	
	public boolean isFinished()
	{
		for (boolean b : donePlayer)
		{
			if (!b) return false;
		}
		return true;
	}
}
