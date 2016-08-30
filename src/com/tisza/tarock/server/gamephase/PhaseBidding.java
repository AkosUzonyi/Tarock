package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseBidding implements GamePhase
{
	private GameSession game;
	private Bidding bidding;
	
	public PhaseBidding(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getCurrentGame().dealing.getCards();
		int bp = game.getCurrentGame().beginnerPlayer;
		bidding = new Bidding(cards, bp);
	}

	public void start()
	{
		onSuccessfulBid();
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketBid)
		{
			PacketBid packetBid = ((PacketBid)packet);
			if (packetBid.getPlayer() == player)
			{
				if (bidding.bid(player, packetBid.getBid()))
				{
					game.broadcastPacket(packetBid);
					onSuccessfulBid();
				}
			}
		}
		else if (packet instanceof PacketThrowCards)
		{
			PlayerCards cards = game.getCurrentGame().dealing.getCards().getPlayerCards(player);
			if (cards.canBeThrown(false))
			{
				game.broadcastPacket(new PacketCardsThrown(player, cards));
				game.startNewGame(true);
			}
		}
	}
	
	private void onSuccessfulBid()
	{
		if (bidding.isFinished())
		{
			game.getCurrentGame().bidding = bidding;
			game.changeGamePhase(new PhaseCalling(game));
		}
		else
		{
			game.broadcastPacket(new PacketTurn(bidding.getNextPlayer(), PacketTurn.Type.BID));
			game.sendPacketToPlayer(bidding.getNextPlayer(), new PacketAvailableBids(bidding.getPossibleBids()));
		}
	}
}
