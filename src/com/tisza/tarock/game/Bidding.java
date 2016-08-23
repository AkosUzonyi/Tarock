package com.tisza.tarock.game;

import java.util.*;

import com.tisza.tarock.card.*;

public class Bidding
{
	private final int beginnerPlayer;
	
	private List<HandCards> playerHands;
	private int currentPlayer;
	
	private int lastBidValue = 4;
	private int lastBidPlayer = -1;
	private boolean isKept = false;
	
	private BidState[] playersState = new BidState[4];
	
	private Invitation invit = null;
	
	public Bidding(List<HandCards> ph, int bp)
	{
		if (ph.size() != 4) throw new IllegalArgumentException();
		
		playerHands = new ArrayList<HandCards>(ph);
		beginnerPlayer = bp;
		currentPlayer = bp;
		
		for (int i = 0; i < playersState.length; i++)
		{
			playersState[i] = BidState.INITIAL;
		}
	}
	
	public int getNextPlayer()
	{
		return currentPlayer;
	}
	
	private boolean canKeep()
	{
		return !isKept && playersState[currentPlayer] == BidState.IN;
	}
	
	private int getDefaultBid()
	{
		return lastBidValue - (canKeep() ? 0 : 1);
	}
	
	public boolean bid(int bid)
	{
		if (isFinished())
			throw new IllegalStateException("Bidding is finished");
		
		if (!getPossibleBids().contains(bid))
			return false;
		
		if (bid == -1)
		{
			if (canKeep() && lastBidValue == 2)
			{
				if (invit != null) throw new Error();
				invit = Invitation.XX;
			}
			
			playersState[currentPlayer] = BidState.OUT;
		}
		else
		{
			int jump = getDefaultBid() - bid;
			
			if (jump == 1)
			{
				if (invit != null) throw new Error();
				invit = Invitation.XIX;
			}
			
			if (jump == 2)
			{
				if (invit != null) throw new Error();
				invit = Invitation.XVIII;
			}
			
			playersState[currentPlayer] = BidState.IN;
			isKept = lastBidValue == bid;
			lastBidPlayer = currentPlayer;
			lastBidValue = bid;
		}
		
		findNextPlayer();
		
		return true;
	}
	
	public List<Integer> getPossibleBids()
	{
		List<Integer> result = new ArrayList<Integer>();
		result.add(-1);
		if (checkBiddingRequirements(currentPlayer))
		{
			int defaultBid = getDefaultBid();
			result.add(defaultBid);
			
			HandCards cards = playerHands.get(currentPlayer);
			boolean canInvit = checkBaseInvitationRequirements(currentPlayer);
			
			if (canKeep() && lastBidValue == 2 && (!canInvit || !cards.hasCard(new TarockCard(20))))
			{
				result.remove((Integer)(-1));
			}
			
			if (canInvit)
			{
				if (cards.hasCard(new TarockCard(19)) && defaultBid - 1 >= 0)
				{
					result.add(defaultBid - 1);
				}
				if (cards.hasCard(new TarockCard(18)) && defaultBid - 2 >= 0)
				{
					result.add(defaultBid - 2);
				}
			}
		}
		return result;
	}
	
	private boolean checkBiddingRequirements(int player)
	{
		for (Card c : playerHands.get(player).getCards())
		{
			if (c.isHonor())
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean checkBaseInvitationRequirements(int player)
	{
		HandCards h = playerHands.get(player);
		return (h.hasCard(new TarockCard(21)) || h.hasCard(new TarockCard(22))) && h.tarockCount() >= 5;
	}
	
	public boolean isFinished()
	{
		return currentPlayer == lastBidPlayer || lastBidValue == 0 && isKept || playersState[currentPlayer] == BidState.OUT;
	}
	
	public int getWinnerPlayer()
	{
		if (!isFinished())
			throw new IllegalStateException("Bidding is in progress");
		return lastBidPlayer;
	}
	
	public int getWinnerBid()
	{
		if (!isFinished())
			throw new IllegalStateException("Bidding is in progress");
		return lastBidValue;
	}
	
	public Invitation getInvitation()
	{
		if (!isFinished())
			throw new IllegalStateException("Bidding is in progress");
		return invit;
	}
	
	//TODO: break infinite loop if all players passz
	private void findNextPlayer()
	{
		int x = currentPlayer;
		do
		{
			currentPlayer++;
			currentPlayer %= 4;
		}
		while (playersState[currentPlayer] == BidState.OUT && currentPlayer != x);
	}
	
	public static enum Invitation
	{
		XVIII, XIX, XX;
	}
	
	private static enum BidState
	{
		INITIAL, IN, OUT;
	}
}

/*

public boolean bid(int bid)
{
	if (isFinished())
		throw new IllegalStateException("Bidding is finished");
	
	if (bid == -1)
	{
		playersState[nextPlayer] = BidState.OUT;
		findNextPlayer();
		return BidResult.OK;
	}
	
	if (bid > 3)
		return BidResult.INVALID_BID;
	
	int jump = getDefaultBid() - bid;
	
	if (jump < 0)
		return BidResult.INVALID_BID;
			
	if (!checkBiddingRequirements(nextPlayer))
		return BidResult.NO_HONOR;
	
	if (jump > 0)
	{
		if (!checkBaseInvitationRequirements(nextPlayer))
			return BidResult.INVALID_INVITATION;
		
		HandCards cards = playerHands.get(nextPlayer);
		
		if (jump == 1)
		{
			if (!cards.hasCard(new TarockCard(19)))
				return BidResult.INVALID_INVITATION;
			
			if (invit != null) throw new Error();
			
			invit = Invitation.XIX;
		}
		
		if (jump == 2)
		{
			if (!cards.hasCard(new TarockCard(18)))
				return BidResult.INVALID_INVITATION;
			
			if (invit != null) throw new Error();
			
			invit = Invitation.XVIII;
		}
		
		if (jump >= 3) //not allowing immediate solo
		{
			return BidResult.INVALID_BID;
		}
	}
	
	playersState[nextPlayer] = BidState.IN;
	isKept = lastBidValue == bid;
	lastBidPlayer = nextPlayer;
	lastBidValue = bid;
	findNextPlayer();
	
	return BidResult.OK;
*/
