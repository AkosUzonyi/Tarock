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
		AllPlayersCards cards = game.getCurrentGame().changing.getCardsAfter();
		int callerPlayer = game.getCurrentGame().bidding.getWinnerPlayer();
		Invitation invit = game.getCurrentGame().bidding.getInvitation();
		int[] tarockSkartedCount = game.getCurrentGame().changing.getTarockCounts();
		calling = new Calling(cards, callerPlayer, invit, tarockSkartedCount);
	}

	public void start()
	{
		game.getCurrentGame().calling = calling;
		
		int caller = calling.getCaller();
		game.sendPacketToPlayer(caller, new PacketAvailableCalls(calling.getCallableCards()));
		game.broadcastPacket(new PacketTurn(caller, PacketTurn.Type.CALL));
	}

	public void playerLoggedIn(int player)
	{
		game.sendPacketToPlayer(player, new PacketPlayerCards(game.getCurrentGame().changing.getCardsAfter().getPlayerCards(player)));
		if (calling.getCaller() == player)
		{
			game.sendPacketToPlayer(player, new PacketAvailableCalls(calling.getCallableCards()));
			game.sendPacketToPlayer(player, new PacketTurn(player, PacketTurn.Type.CALL));
		}
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
				else
				{
					if (player == calling.getCaller())
					{
						game.broadcastPacket(new PacketTurn(player, PacketTurn.Type.CALL));
					}
				}
			}
		}
	}
}
