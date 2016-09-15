package com.tisza.tarock.server.gamephase;

import java.util.*;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseChanging implements GamePhase
{
	private GameSession game;
	private Changing changing;
	
	public PhaseChanging(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getCurrentGame().dealing.getCards();
		List<Card> talon = game.getCurrentGame().dealing.getTalon();
		int winnerPlayer = game.getCurrentGame().bidding.getWinnerPlayer();
		int winnerBid = game.getCurrentGame().bidding.getWinnerBid();
		changing = new Changing(cards, talon, winnerPlayer, winnerBid);
	}

	public void start()
	{
		game.getCurrentGame().changing = changing;
		game.broadcastPacket(new PacketPhase(PacketPhase.Phase.CHANGING));
		
		for (int i = 0; i < 4; i++)
		{
			List<Card> cards = changing.getCardsObtainedFromTalon(i);
			game.sendPacketToPlayer(i, new PacketChange(cards, i));
			game.sendPacketToPlayer(i, new PacketTurn(i));
		}
	}

	public void playerLoggedIn(int player)
	{
		if (changing.isDone(player))
		{
			game.sendPacketToPlayer(player, new PacketPlayerCards(game.getCurrentGame().changing.getCardsAfter().getPlayerCards(player)));
			game.sendPacketToPlayer(player, new PacketPhase(PacketPhase.Phase.CHANGING));
		}
		else
		{
			game.sendPacketToPlayer(player, new PacketPlayerCards(game.getCurrentGame().dealing.getCards().getPlayerCards(player)));
			game.sendPacketToPlayer(player, new PacketPhase(PacketPhase.Phase.CHANGING));
			game.sendPacketToPlayer(player, new PacketChange(changing.getCardsObtainedFromTalon(player), player));
			game.sendPacketToPlayer(player, new PacketTurn(player));
		}
		
		for (int i = 0; i < 4; i++)
		{
			if (changing.isDone(i))
			{
				game.sendPacketToPlayer(player, new PacketChangeDone(i));
			}
		}
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketChange)
		{
			PacketChange packetChange = ((PacketChange)packet);
			if (packetChange.getPlayer() == player)
			{
				if (changing.skart(player, packetChange.getCards()))
				{
					game.sendPacketToPlayer(player, new PacketPlayerCards(changing.getCardsAfter().getPlayerCards(player)));
					game.broadcastPacket(new PacketChangeDone(player));
					
					if (changing.isFinished())
					{
						game.broadcastPacket(new PacketSkartTarock(changing.getTarockCounts()));
						game.changeGamePhase(new PhaseCalling(game));
					}
				}
				else
				{
					game.sendPacketToPlayer(player, new PacketTurn(player));
				}
			}
		}
		else if (packet instanceof PacketThrowCards)
		{
			PlayerCards cards = game.getCurrentGame().dealing.getCards().getPlayerCards(player);
			if (cards.canBeThrown(true))
			{
				game.broadcastPacket(new PacketCardsThrown(player, cards));
				game.changeGamePhase(new PhasePendingNewGame(game, true));
			}
		}
	}
}
