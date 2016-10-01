package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseGameplay implements GamePhase
{
	private GameSession game;
	private Gameplay gameplay;
	
	public PhaseGameplay(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getCurrentGame().changing.getCardsAfter();
		int bp = game.getCurrentGame().beginnerPlayer;
		gameplay = new Gameplay(cards, bp);
	}

	public void start()
	{
		game.getCurrentGame().gameplay = gameplay;
		game.broadcastPacket(new PacketPhase(PacketPhase.Phase.GAMEPLAY));
		onSuccessfulPlayCard();
	}

	public void playerLoggedIn(int player)
	{
		game.sendPacketToPlayer(player, new PacketPlayerCards(game.getCurrentGame().gameplay.getPlayerCards().getPlayerCards(player)));
		game.sendPacketToPlayer(player, new PacketPhase(PacketPhase.Phase.GAMEPLAY));
		
		Round currentRound = gameplay.getCurrentRound();
		int bp = currentRound.getBeginnerPlayer();
		int np = currentRound.getCurrentPlayer();
		for (int i = 0; i < (np + 4 - bp) % 4; i++)
		{
			int p = (i + bp) % 4;
			game.sendPacketToPlayer(player, new PacketTurn(gameplay.getCurrentPlayer()));
			game.sendPacketToPlayer(player, new PacketPlayCard(currentRound.getCards().get(p), p));
		}
		game.sendPacketToPlayer(player, new PacketTurn(gameplay.getCurrentPlayer()));
	}
	
	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketPlayCard)
		{
			Round round = gameplay.getCurrentRound();
			PacketPlayCard packetPlayCard = ((PacketPlayCard)packet);
			if (packetPlayCard.getPlayer() == player)
			{
				if (gameplay.playCard(packetPlayCard.getCard(), player))
				{
					game.broadcastPacket(packetPlayCard);
					if (round.isFinished())
					{
						game.broadcastPacket(new PacketCardsTaken(round.getWinner()));
					}
					onSuccessfulPlayCard();
				}
				else
				{
					game.broadcastPacket(new PacketTurn(gameplay.getCurrentPlayer()));
				}
			}
		}
	}

	private void onSuccessfulPlayCard()
	{
		if (gameplay.isFinished())
		{
			game.changeGamePhase(new PhaseEnd(game));
		}
		else
		{
			game.broadcastPacket(new PacketTurn(gameplay.getCurrentPlayer()));
		}
	}
}
