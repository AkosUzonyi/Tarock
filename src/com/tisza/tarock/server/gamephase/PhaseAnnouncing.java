package com.tisza.tarock.server.gamephase;

import com.tisza.tarock.game.*;
import com.tisza.tarock.game.Bidding.Invitation;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseAnnouncing implements GamePhase
{
	private GameSession game;
	private Announcing announcing;
	
	public PhaseAnnouncing(GameSession g)
	{
		game = g;
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		Invitation invit = game.getCurrentGame().calling.getInvitationAccepted();
		announcing = new Announcing(game.getCurrentGame().changing.getCardsAfter(), pp, invit);
	}

	public void start()
	{
		game.getCurrentGame().announcing = announcing;
		onAnnounced();
	}

	public void packetFromPlayer(int player, Packet packet)
	{
		if (packet instanceof PacketAnnounce)
		{
			PacketAnnounce packetAnnounce = ((PacketAnnounce)packet);
			if (packetAnnounce.getPlayer() == player)
			{
				if (announcing.announce(player, packetAnnounce.getAnnouncement()))
				{
					game.broadcastPacket(packetAnnounce);
				}
				onAnnounced();
			}
		}
	}
	
	private void onAnnounced()
	{
		if (announcing.isFinished())
		{
			game.changeGamePhase(new PhaseGameplay(game));
		}
		else
		{
			game.broadcastPacket(new PacketTurn(announcing.getNextPlayer(), PacketTurn.Type.ANNOUNCE));
			game.sendPacketToPlayer(announcing.getNextPlayer(), new PacketAvailabeAnnouncements(announcing.getAvailableAnnouncements()));
		}
	}
}
