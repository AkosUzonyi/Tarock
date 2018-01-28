package com.tisza.tarock.game;

import com.tisza.tarock.card.Card;
import com.tisza.tarock.card.PlayerCards;
import com.tisza.tarock.card.TarockCard;
import com.tisza.tarock.card.filter.TarockFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bidding extends Phase
{
	private int currentPlayer;
	private int lastBidValue = 4;
	private int lastBidPlayer = -1;
	private boolean isKept = false;
	
	private BidState[] playersState = new BidState[4];
	
	public Bidding(GameState gs)
	{
		super(gs);
		
		Arrays.fill(playersState, BidState.INITIAL);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.BIDDING;
	}
	
	public void onStart()
	{
		currentPlayer = gameState.getBeginnerPlayer();
		sendAvailableBids();
	}

	private boolean canKeep()
	{
		return !isKept && playersState[currentPlayer] == BidState.IN;
	}
	
	private int getDefaultBid()
	{
		return lastBidValue - (canKeep() ? 0 : 1);
	}
	
	public boolean bid(int player, int bid)
	{
		if (player != currentPlayer)
			return false;
		
		if (!getAvailableBids().contains(bid))
			return false;
		
		if (bid == -1)
		{
			if (canKeep() && lastBidValue == 2)
			{
				gameState.setInvitationSent(Invitation.XX, currentPlayer);
			}
			
			playersState[currentPlayer] = BidState.OUT;
		}
		else
		{
			int jump = getDefaultBid() - bid;
			
			if (jump == 1)
			{
				gameState.setInvitationSent(Invitation.XIX, currentPlayer);
			}
			
			if (jump == 2)
			{
				gameState.setInvitationSent(Invitation.XVIII, currentPlayer);
			}
			
			playersState[currentPlayer] = BidState.IN;
			isKept = lastBidValue == bid;
			lastBidPlayer = currentPlayer;
			lastBidValue = bid;
		}

		findNextPlayer();
		
		if (!isFinished())
		{
			sendAvailableBids();
		}
		else
		{
			if (lastBidPlayer < 0)
			{
				gameState.changePhase(new PendingNewGame(gameState, true));
			}
			else
			{
				gameState.setBidResult(lastBidPlayer, lastBidValue);
				gameState.changePhase(new Changing(gameState));
			}
		}

		return true;
	}
	
	public boolean throwCards(int player)
	{
		PlayerCards cards = gameState.getPlayerCards(player);
		if (cards.canBeThrown(false))
		{
			gameState.changePhase(new PendingNewGame(gameState, true));
			return true;
		}
		return false;
	}
	
	private List<Integer> getAvailableBids()
	{
		if (isFinished())
			throw new IllegalStateException();
		
		List<Integer> result = new ArrayList<Integer>();
		result.add(-1);
		if (checkBiddingRequirements(currentPlayer))
		{
			int defaultBid = getDefaultBid();
			result.add(defaultBid);
			
			PlayerCards cards = gameState.getPlayerCards(currentPlayer);
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
	
	private void sendAvailableBids()
	{
		gameState.getEventQueue().toPlayer(currentPlayer).availabeBids(getAvailableBids());
		gameState.getEventQueue().broadcast().turn(currentPlayer);
	}

	private boolean checkBiddingRequirements(int player)
	{
		for (Card c : gameState.getPlayerCards(player).getCards())
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
		PlayerCards h = gameState.getPlayerCards(player);
		return (h.hasCard(new TarockCard(21)) || h.hasCard(new TarockCard(22))) && h.filter(new TarockFilter()).size() >= 5;
	}
	
	private boolean isFinished()
	{
		return currentPlayer == lastBidPlayer || lastBidValue == 0 && isKept || playersState[currentPlayer] == BidState.OUT;
	}
	
	private void findNextPlayer()
	{
		for (int i = 0; i < 4; i++)
		{
			int p = (currentPlayer + i + 1) % 4;
			if (playersState[p] != BidState.OUT)
			{
				currentPlayer = p;
				break;
			}
		}
	}
	
	public static enum Invitation
	{
		NONE, XVIII, XIX, XX;
	}
	
	private static enum BidState
	{
		INITIAL, IN, OUT;
	}
}
