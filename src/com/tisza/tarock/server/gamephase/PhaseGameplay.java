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
		onSuccessfulPlayCard();
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketPlayCard)
		{
			PacketPlayCard packetPlayCard = ((PacketPlayCard)packet);
			if (packetPlayCard.getPlayer() == player)
			{
				if (gameplay.playCard(packetPlayCard.getCard(), player));
				{
					game.broadcastPacket(packetPlayCard);
					onSuccessfulPlayCard();
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
			game.broadcastPacket(new PacketTurn(gameplay.getNextPlayer(), PacketTurn.Type.PLAY_CARD));
		}
	}
}
