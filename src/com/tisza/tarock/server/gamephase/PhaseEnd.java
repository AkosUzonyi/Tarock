package com.tisza.tarock.server.gamephase;

import java.util.*;

import com.tisza.tarock.announcement.*;
import com.tisza.tarock.game.*;
import com.tisza.tarock.net.packet.*;
import com.tisza.tarock.server.*;

public class PhaseEnd implements GamePhase
{
	private GameSession game;
	
	public PhaseEnd(GameSession g)
	{
		game = g;
	}
	
	public void start()
	{
		int pointsForCallerTeam = 0;
		
		Gameplay gp = game.getCurrentGame().gameplay;
		PlayerPairs pp = game.getCurrentGame().calling.getPlayerPairs();
		int winnerBid = game.getCurrentGame().bidding.getWinnerBid();
		
		Map<Announcement, AnnouncementState> announcementStates = game.getCurrentGame().announcing.getAnnouncementStates();
		for (Map.Entry<Announcement, AnnouncementState> announcementEntry : announcementStates.entrySet())
		{
			Announcement a = announcementEntry.getKey();
			AnnouncementState as = announcementEntry.getValue();
			for (Team t : Team.values())
			{
				AnnouncementState.PerTeam aspt = as.team(t);
				int contraLevel = aspt.isAnnounced() ? aspt.getContraLevel() : 0;
				int points = a.calculatePoints(gp, pp, t, winnerBid, aspt.isAnnounced()) * (int)Math.pow(2, contraLevel);
				pointsForCallerTeam += points * (t == Team.CALLER ? 1 : -1);
				
				List<PacketAnnouncementStatistics.Entry> statEntriesForTeam = new ArrayList<PacketAnnouncementStatistics.Entry>();
				
				if (a instanceof AnnouncementBase)
				{
					AnnouncementBase ab = (AnnouncementBase)a;
					AnnouncementBase.Result result = ab.isSuccessful(gp, pp, t);
					statEntriesForTeam.add(new PacketAnnouncementStatistics.Entry(a, contraLevel, result, points));
				}
				
				for (int player : pp.getPlayersInTeam(t))
				{
					game.sendPacketToPlayer(player, new PacketAnnouncementStatistics(statEntriesForTeam));
				}
			}
		}
		
		game.getPoints().addPoints(pointsForCallerTeam, pp.getCaller(), pp.getCalled());
		game.savePoints();
		game.broadcastPacket(new PacketPoints(game.getPoints().getCurrentPoints()));
		game.changeGamePhase(new PhasePendingNewGame(game, false));
	}

	public void packetFromPlayer(int player, Packet packet)
	{
	}

}
