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
		for (int i = 0; i < 4; i++)
		{
			List<Card> cards = changing.getCardsObtainedFromTalon(i);
			game.sendPacketToPlayer(i, new PacketChange(cards, i));
			game.sendPacketToPlayer(i, new PacketTurn(i, PacketTurn.Type.CHANGE));
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
					game.broadcastPacket(new PacketChangeDone(player));
					
					if (changing.isFinished())
					{
						game.broadcastPacket(new PacketSkartTarock(changing.getTarockCounts()));
						game.changeGamePhase(new PhaseCalling(game));
					}
				}
				else
				{
					game.sendPacketToPlayer(player, new PacketTurn(player, PacketTurn.Type.CHANGE));
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
