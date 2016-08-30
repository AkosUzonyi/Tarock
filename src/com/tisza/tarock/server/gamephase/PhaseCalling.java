package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.game.Bidding.Invitation;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseCalling implements GamePhase
{
	private GameSession game;
	private Calling calling;
	
	public PhaseCalling(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getGameHistory().dealing.getCards();
		int callerPlayer = game.getGameHistory().bidding.getWinnerPlayer();
		Invitation invit = game.getGameHistory().bidding.getInvitation();
		calling = new Calling(cards, callerPlayer, invit);
	}

	public void start()
	{
		game.sendPacketToPlayer(calling.getCaller(), new PacketAvailableCalls(calling.getCallableCards()));
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketCall)
		{
			PacketCall packetCall = ((PacketCall)packet);
			if (packetCall.getPlayer() == player)
			{
				if (calling.call(player, packetCall.getCalledCard()))
				{
					game.broadcastPacket(packetCall);
					if (calling.isFinished())
					{
						game.getGameHistory().calling = calling;
						game.changeGamePhase(new PhaseAnnouncing(game));
					}
				}
			}
		}
	}
}
