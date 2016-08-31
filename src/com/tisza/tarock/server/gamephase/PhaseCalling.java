package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.card.*;
import com.tisza.tarock.game.Bidding.Invitation;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseCalling implements GamePhase
{
	private GameSession game;
	private Calling calling;
	
	public PhaseCalling(GameSession g)
	{
		game = g;
		AllPlayersCards cards = game.getCurrentGame().dealing.getCards();
		int callerPlayer = game.getCurrentGame().bidding.getWinnerPlayer();
		Invitation invit = game.getCurrentGame().bidding.getInvitation();
		int[] tarockSkartedCount = game.getCurrentGame().changing.getTarockCounts();
		calling = new Calling(cards, callerPlayer, invit, tarockSkartedCount);
	}

	public void start()
	{
		game.getCurrentGame().calling = calling;
		
		int caller = calling.getCaller();
		game.broadcastPacket(new PacketTurn(caller, PacketTurn.Type.CALL));
		game.sendPacketToPlayer(caller, new PacketAvailableCalls(calling.getCallableCards()));
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
						game.changeGamePhase(new PhaseAnnouncing(game));
					}
				}
			}
		}
	}
}
