package com.tisza.tarock.game.phase;

import com.tisza.tarock.game.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.message.*;

import java.util.*;

class Bidding extends Phase
{
	private PlayerSeat currentPlayer;
	private PlayerSeat lastBidPlayer = null;
	private int lastBidValue = 4;
	private boolean isKept = false;
	
	private PlayerSeat.Map<BidState> playersState = new PlayerSeat.Map<>(BidState.INITIAL);
	
	public Bidding(GameState game)
	{
		super(game);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.BIDDING;
	}
	
	@Override
	public void onStart()
	{
		currentPlayer = game.getBeginnerPlayer();
		sendAvailableBids();
	}

	@Override
	public void requestHistory(PlayerSeat player, EventSender eventSender)
	{
		super.requestHistory(player, eventSender);

		eventSender.turn(currentPlayer);
		if (player == currentPlayer)
			sendAvailableBids();
	}

	private boolean canKeep()
	{
		return !isKept && playersState.get(currentPlayer) == BidState.IN;
	}
	
	private int getDefaultBid()
	{
		return lastBidValue - (canKeep() ? 0 : 1);
	}
	
	@Override
	public void bid(PlayerSeat player, int bid)
	{
		if (player != currentPlayer)
			return;
		
		if (!getAvailableBids().contains(bid))
			return;
		
		if (bid == -1)
		{
			if (canKeep() && lastBidValue == 2)
			{
				game.setInvitationSent(Invitation.XX, currentPlayer);
			}
			
			playersState.put(currentPlayer, BidState.OUT);
		}
		else
		{
			int jump = getDefaultBid() - bid;

			if (jump == 1 && game.getInvitSent() != Invitation.XIX)
			{
				game.setInvitationSent(Invitation.XIX, currentPlayer);
			}

			if (jump == 2 && game.getInvitSent() != Invitation.XVIII)
			{
				game.setInvitationSent(Invitation.XVIII, currentPlayer);
			}

			playersState.put(currentPlayer, BidState.IN);
			isKept = lastBidValue == bid;
			lastBidPlayer = currentPlayer;
			lastBidValue = bid;
		}

		history.registerBid(player, bid);
		game.getBroadcastEventSender().bid(player, bid);

		findNextPlayer();
		
		if (!isFinished())
		{
			sendAvailableBids();
		}
		else
		{
			if (lastBidPlayer == null)
			{
				game.changePhase(new PendingNewGame(game, true));
			}
			else
			{
				game.setBidResult(lastBidPlayer, lastBidValue);
				game.changePhase(new Changing(game));
			}
		}
	}
	
	@Override
	public void throwCards(PlayerSeat player)
	{
		if (!game.getPlayerCards(player).canBeThrown())
			return;

		game.getBroadcastEventSender().throwCards(player);
		game.changePhase(new PendingNewGame(game, true));
	}

	public PlayerSeat getCurrentPlayer()
	{
		return currentPlayer;
	}

	public List<Integer> getAvailableBids()
	{
		if (isFinished())
			throw new IllegalStateException();

		List<Integer> result = new ArrayList<>();

		if (playersState.get(currentPlayer) == BidState.OUT)
			return result;
		
		result.add(-1);
		if (checkBiddingRequirements(currentPlayer))
		{
			int defaultBid = getDefaultBid();
			if (defaultBid >= 0)
				result.add(defaultBid);
			
			PlayerCards cards = game.getPlayerCards(currentPlayer);
			boolean canInvit = checkBaseInvitationRequirements(currentPlayer);
			
			if (canKeep() && lastBidValue == 2 && (!canInvit || !cards.hasCard(Card.getTarockCard(20))))
			{
				result.remove((Integer)(-1));
			}

			for (int jump = 1; jump <= 2; jump++)
			{
				Invitation invit = jump == 1 ? Invitation.XIX : Invitation.XVIII;
				int jumpBid = defaultBid - jump;

				if (jumpBid < 0)
					continue;

				if (canInvit && cards.hasCard(invit.getCard()) || game.getInvitSent() == invit)
					result.add(jumpBid);
			}

			if (defaultBid == 3)
				result.add(0);
		}
		return result;
	}
	
	private void sendAvailableBids()
	{
		game.getPlayerEventSender(currentPlayer).availabeBids(getAvailableBids());
		game.getBroadcastEventSender().turn(currentPlayer);
	}

	private boolean checkBiddingRequirements(PlayerSeat player)
	{
		for (Card c : game.getPlayerCards(player).getCards())
		{
			if (c.isHonor())
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean checkBaseInvitationRequirements(PlayerSeat player)
	{
		PlayerCards cards = game.getPlayerCards(player);
		return (cards.hasCard(Card.getTarockCard(21)) || cards.hasCard(Card.getTarockCard(22))) && cards.getTarockCount() >= 5;
	}
	
	private boolean isFinished()
	{
		return currentPlayer == lastBidPlayer || lastBidValue == 0 && isKept || playersState.get(currentPlayer) == BidState.OUT;
	}
	
	private void findNextPlayer()
	{
		for (PlayerSeat p = currentPlayer.nextPlayer(); p != currentPlayer; p = p.nextPlayer())
		{
			if (playersState.get(p) == BidState.OUT)
				continue;

			if (lastBidValue == 0 && playersState.get(p) != BidState.IN)
				continue;

			currentPlayer = p;
			break;
		}
	}

	private static enum BidState
	{
		INITIAL, IN, OUT;
	}
}
