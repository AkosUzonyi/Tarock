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
	
	public Bidding(GameSession gameSession)
	{
		super(gameSession);
		
		Arrays.fill(playersState, BidState.INITIAL);
	}
	
	public PhaseEnum asEnum()
	{
		return PhaseEnum.BIDDING;
	}
	
	public void onStart()
	{
		currentPlayer = currentGame.getBeginnerPlayer();
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
	
	public void bid(int player, int bid)
	{
		if (player != currentPlayer)
			return;
		
		if (!getAvailableBids().contains(bid))
			return;
		
		if (bid == -1)
		{
			if (canKeep() && lastBidValue == 2)
			{
				currentGame.setInvitationSent(Invitation.XX, currentPlayer);
			}
			
			playersState[currentPlayer] = BidState.OUT;
		}
		else
		{
			int jump = getDefaultBid() - bid;
			
			if (jump == 1)
			{
				currentGame.setInvitationSent(Invitation.XIX, currentPlayer);
			}
			
			if (jump == 2)
			{
				currentGame.setInvitationSent(Invitation.XVIII, currentPlayer);
			}
			
			playersState[currentPlayer] = BidState.IN;
			isKept = lastBidValue == bid;
			lastBidPlayer = currentPlayer;
			lastBidValue = bid;
		}

		gameSession.getBroadcastEventSender().bid(player, bid);

		findNextPlayer();
		
		if (!isFinished())
		{
			sendAvailableBids();
		}
		else
		{
			if (lastBidPlayer < 0)
			{
				gameSession.changePhase(new PendingNewGame(gameSession, true));
			}
			else
			{
				currentGame.setBidResult(lastBidPlayer, lastBidValue);
				gameSession.changePhase(new Changing(gameSession));
			}
		}
	}
	
	public void throwCards(int player)
	{
		PlayerCards cards = currentGame.getPlayerCards(player);
		if (cards.canBeThrown(false))
		{
			gameSession.getBroadcastEventSender().throwCards(player);
			gameSession.changePhase(new PendingNewGame(gameSession, true));
		}
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
			
			PlayerCards cards = currentGame.getPlayerCards(currentPlayer);
			boolean canInvit = checkBaseInvitationRequirements(currentPlayer);
			
			if (canKeep() && lastBidValue == 2 && (!canInvit || !cards.hasCard(Card.getTarockCard(20))))
			{
				result.remove((Integer)(-1));
			}
			
			if (canInvit)
			{
				if (cards.hasCard(Card.getTarockCard(19)) && defaultBid - 1 >= 0)
				{
					result.add(defaultBid - 1);
				}
				if (cards.hasCard(Card.getTarockCard(18)) && defaultBid - 2 >= 0)
				{
					result.add(defaultBid - 2);
				}
			}
		}
		return result;
	}
	
	private void sendAvailableBids()
	{
		gameSession.getPlayerEventQueue(currentPlayer).availabeBids(getAvailableBids());
		gameSession.getBroadcastEventSender().turn(currentPlayer);
	}

	private boolean checkBiddingRequirements(int player)
	{
		for (Card c : currentGame.getPlayerCards(player).getCards())
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
		PlayerCards h = currentGame.getPlayerCards(player);
		return (h.hasCard(Card.getTarockCard(21)) || h.hasCard(Card.getTarockCard(22))) && h.filter(new TarockFilter()).size() >= 5;
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
