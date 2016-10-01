package com.tisza.tarock.server.gamephase;

import java.util.*;

import com.tisza.tarock.card.*;
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
		AllPlayersCards cards = game.getCurrentGame().changing.getCardsAfter();
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		Invitation invitAccepted = game.getCurrentGame().calling.getInvitationAccepted();
		int playerToAnnounceSolo = game.getCurrentGame().calling.getPlayerToAnnounceSolo();
		announcing = new Announcing(cards, pp, invitAccepted, playerToAnnounceSolo);
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
		if (announcing.getCurrentPlayer() == player)
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
		else if (packet instanceof PacketAnnouncePassz)
		{
			PacketAnnouncePassz packetAnnouncePassz = ((PacketAnnouncePassz)packet);
			if (packetAnnouncePassz.getPlayer() == player)
			{
				if (announcing.passz(player))
				{
					game.broadcastPacket(packetAnnouncePassz);
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
			game.sendPacketToPlayer(announcing.getCurrentPlayer(), new PacketAvailableAnnouncements(getAvailableAnnouncementsShown()));
			game.broadcastPacket(new PacketTurn(announcing.getCurrentPlayer()));
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
