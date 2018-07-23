package com.tisza.tarock.game;

import com.tisza.tarock.card.*;

import java.util.*;

class Bidding extends Phase
{
	private PlayerSeat currentPlayer;
	private PlayerSeat lastBidPlayer = null;
	private int lastBidValue = 4;
	private boolean isKept = false;
	
	private PlayerSeat.Map<BidState> playersState = new PlayerSeat.Map<>(BidState.INITIAL);
	
	public Bidding(GameSession gameSession)
	{
		super(gameSession);
	}
	
	@Override
	public PhaseEnum asEnum()
	{
		return PhaseEnum.BIDDING;
	}
	
	@Override
	public void onStart()
	{
		currentPlayer = currentGame.getBeginnerPlayer();
		sendAvailableBids();
	}

	@Override
	public void requestHistory(PlayerSeat player)
	{
		super.requestHistory(player);

		gameSession.getPlayerEventSender(player).turn(currentPlayer);
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
				currentGame.setInvitationSent(Invitation.XX, currentPlayer);
			}
			
			playersState.put(currentPlayer, BidState.OUT);
		}
		else
		{
			int jump = getDefaultBid() - bid;

			if (jump == 1 && currentGame.getInvitSent() != Invitation.XIX)
			{
				currentGame.setInvitationSent(Invitation.XIX, currentPlayer);
			}

			if (jump == 2 && currentGame.getInvitSent() != Invitation.XVIII)
			{
				currentGame.setInvitationSent(Invitation.XVIII, currentPlayer);
			}

			playersState.put(currentPlayer, BidState.IN);
			isKept = lastBidValue == bid;
			lastBidPlayer = currentPlayer;
			lastBidValue = bid;
		}

		history.registerBid(player, bid);
		gameSession.getBroadcastEventSender().bid(player, bid);

		findNextPlayer();
		
		if (!isFinished())
		{
			sendAvailableBids();
		}
		else
		{
			if (lastBidPlayer == null)
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
	
	@Override
	public void throwCards(PlayerSeat player)
	{
		PlayerCards cards = currentGame.getPlayerCards(player);
		if (cards.canBeThrown(false))
		{
			gameSession.getBroadcastEventSender().throwCards(player);
			gameSession.changePhase(new PendingNewGame(gameSession, true));
		}
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
			
			PlayerCards cards = currentGame.getPlayerCards(currentPlayer);
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

				if (canInvit && cards.hasCard(invit.getCard()) || currentGame.getInvitSent() == invit)
					result.add(jumpBid);
			}
		}
		return result;
	}
	
	private void sendAvailableBids()
	{
		gameSession.getPlayerEventSender(currentPlayer).availabeBids(getAvailableBids());
		gameSession.getBroadcastEventSender().turn(currentPlayer);
	}

	private boolean checkBiddingRequirements(PlayerSeat player)
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
	
	private boolean checkBaseInvitationRequirements(PlayerSeat player)
	{
		PlayerCards cards = currentGame.getPlayerCards(player);
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
			if (playersState.get(p) != BidState.OUT)
			{
				currentPlayer = p;
				break;
			}
		}
	}
	
	public static enum Invitation
	{
		NONE, XVIII, XIX, XX;

		public Card getCard()
		{
			switch (this)
			{
				case XVIII: return Card.getTarockCard(18);
				case XIX: return Card.getTarockCard(19);
				case XX: return Card.getTarockCard(20);
			}
			return null;
		}
	}
	
	private static enum BidState
	{
		INITIAL, IN, OUT;
	}
}
