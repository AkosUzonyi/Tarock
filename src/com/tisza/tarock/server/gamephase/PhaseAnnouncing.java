package com.tisza.tarock.server.gamephase;

import java.util.*;

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
		game.broadcastPacket(new PacketPhase(PacketPhase.Phase.ANNOUNCING));
		onAnnounced();
	}

	public void playerLoggedIn(int player)
	{
		game.sendPacketToPlayer(player, new PacketPlayerCards(game.getCurrentGame().changing.getCardsAfter().getPlayerCards(player)));
		game.sendPacketToPlayer(player, new PacketPhase(PacketPhase.Phase.ANNOUNCING));		
		if (announcing.getNextPlayer() == player)
		{
			game.sendPacketToPlayer(player, new PacketAvailableAnnouncements(getAvailableAnnouncementsShown()));
			game.sendPacketToPlayer(player, new PacketTurn(player));
		}
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
			game.sendPacketToPlayer(announcing.getNextPlayer(), new PacketAvailableAnnouncements(getAvailableAnnouncementsShown()));
			game.broadcastPacket(new PacketTurn(announcing.getNextPlayer()));
		}
	}
	
	private List<AnnouncementContra> getAvailableAnnouncementsShown()
	{
		List<AnnouncementContra> result = new ArrayList<AnnouncementContra>();
		for (AnnouncementContra ac : announcing.getAvailableAnnouncements())
		{
			if (ac.getAnnouncement().isShownToUser())
			{
				result.add(ac);
			}
		}
		return result;
	}
}
